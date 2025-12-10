#!/bin/bash
#$ -l h_rt=48:00:00
#$ -N pokemon_qlearn
#$ -j y
#$ -o pokemon_training.log
#$ -pe omp 8

# Pokemon Q-Learning Training Script for SCC
# Assignment due: 12/10/2025
# This script trains for 48 hours with optimized hyperparameters

module load java/11

# Change this to your directory!
cd /projectnb/cs440/YOUR_USERNAME/cs440

# CRITICAL: Set gamma to 0.99 (default 0.0001 is TOO LOW!)
# Using adam optimizer (much better than SGD)
# Training for 2000 cycles with 150 games/cycle

java -cp "./lib/*:." edu.bu.pas.pokemon.ParallelTrain \
  edu.bu.pas.pokemon.agents.AggroAgent \
  --numCycles 2000 \
  --numTrainingGames 150 \
  --numEvalGames 30 \
  --maxBufferSize 3840 \
  --miniBatchSize 384 \
  --numUpdates 3 \
  --lr 0.0001 \
  --gamma 0.99 \
  --optimizerType adam \
  --beta1 0.9 \
  --beta2 0.999 \
  --clip 1.0 \
  --numThreads 6 \
  --outFile /projectnb/cs440/YOUR_USERNAME/params/qFunction \
  | tee /projectnb/cs440/YOUR_USERNAME/training.log

echo "Training complete! Check training.log for results."
echo "Models saved to params/qFunction*.model"
echo "Select the model with highest avg(num_wins) and rename to params.model"
