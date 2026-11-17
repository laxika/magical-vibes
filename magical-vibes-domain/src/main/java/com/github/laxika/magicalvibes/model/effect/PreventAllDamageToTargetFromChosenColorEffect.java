package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, choose a color and prevent all damage from that color to the target this turn.
 *
 * <p>As a floating effect (CR 611.2a, CR 615) it names the colour the resolution chose.
 *
 * @param creatureOnly whether the target must be a creature
 * @param chosenColor  the colour chosen on resolution; {@code null} on the effect a card carries
 */
public record PreventAllDamageToTargetFromChosenColorEffect(boolean creatureOnly,
                                                            com.github.laxika.magicalvibes.model.CardColor chosenColor)
        implements CardEffect {

    public PreventAllDamageToTargetFromChosenColorEffect(boolean creatureOnly) {
        this(creatureOnly, null);
    }

    public PreventAllDamageToTargetFromChosenColorEffect() {
        this(false, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(creatureOnly ? TargetPredicates.creature() : TargetPredicates.anyTarget());
    }
}
