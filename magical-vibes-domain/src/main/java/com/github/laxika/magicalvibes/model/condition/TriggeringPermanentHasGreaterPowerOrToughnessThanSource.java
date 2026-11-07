package com.github.laxika.magicalvibes.model.condition;

/**
 * "If that creature has greater power or toughness than this creature": the permanent the trigger event was about has
 * greater power or greater toughness than the ability's source (evolve, CR 702.100a, and the cards written like it).
 * Each permanent is read as it is on the battlefield, or as it last existed there once it has left (CR 608.2h); one
 * that isn't a creature never compares as greater or smaller (CR 702.100c).
 */
public record TriggeringPermanentHasGreaterPowerOrToughnessThanSource() implements Condition {

    @Override
    public String conditionName() {
        return "triggering creature has greater power or toughness than source";
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering creature does not have greater power or toughness than source";
    }
}
