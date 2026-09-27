package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.VotingResult;

/** Model of Unity's trigger, carrying the vote snapshot captured when players finish voting. */
public record ModelOfUnityEffect(VotingResult votingResult) implements CardEffect {

    public ModelOfUnityEffect() {
        this(null);
    }
}
