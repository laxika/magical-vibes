package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.VotingResult;

/** Erestor's trigger, carrying the vote snapshot captured when players finished voting. */
public record ErestorOfTheCouncilEffect(VotingResult votingResult) implements CardEffect {

    public ErestorOfTheCouncilEffect() {
        this(null);
    }
}
