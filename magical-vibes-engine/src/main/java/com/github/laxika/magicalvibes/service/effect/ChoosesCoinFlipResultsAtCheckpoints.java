package com.github.laxika.magicalvibes.service.effect;

/**
 * Marks an effect handler that flips coins again after changing the game, and that can be resolved
 * again from its last coin-flip checkpoint. Like {@link ChoosesCoinFlipResultsFirst}, its first
 * coin-flip call may stop it for a Krark's Thumb choice; in addition, after each change it calls
 * {@code CoinFlipService.checkpoint} with its own effect and a resume point, which reopens the choice
 * window for its next coin-flip call. When such a call stops it, the handler is resolved again,
 * reads {@code CoinFlipService.resumePoint} to skip what it already did, and reads the flips made since
 * the checkpoint back with the kept results. Between a checkpoint and the next coin-flip call the
 * handler must change nothing, and the resume point must be all it needs to continue.
 */
public interface ChoosesCoinFlipResultsAtCheckpoints {
}
