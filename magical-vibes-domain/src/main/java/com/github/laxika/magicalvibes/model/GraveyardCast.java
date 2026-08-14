package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * "You may cast this card from your graveyard." Uses the card's normal mana cost
 * (unless {@code alternateManaCost} overrides it) and does not exile after resolution
 * (unlike flashback). The card goes to the graveyard normally if it dies or is countered,
 * allowing repeated graveyard casts.
 *
 * @param controllerControlsPredicate optional condition requiring the casting
 *                                     player to control a matching permanent
 * @param alternateManaCost           optional mana cost paid <em>rather than</em> the card's normal
 *                                     mana cost when cast from the graveyard (e.g. Worldheart Phoenix's
 *                                     "by paying {W}{U}{B}{R}{G}"); {@code null} = pay the normal cost
 * @param additionalCosts              additional costs that must be paid when using this permission
 */
public record GraveyardCast(PermanentPredicate controllerControlsPredicate, String alternateManaCost,
                            List<CastingCost> additionalCosts) implements CastingOption {

    public GraveyardCast {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
    }

    public GraveyardCast() {
        this(null, null, List.of());
    }

    public GraveyardCast(PermanentPredicate controllerControlsPredicate) {
        this(controllerControlsPredicate, null, List.of());
    }

    public GraveyardCast(String alternateManaCost) {
        this(null, alternateManaCost, List.of());
    }

    public GraveyardCast(PermanentPredicate controllerControlsPredicate, String alternateManaCost) {
        this(controllerControlsPredicate, alternateManaCost, List.of());
    }

    public GraveyardCast(List<CastingCost> additionalCosts) {
        this(null, null, additionalCosts);
    }

    public GraveyardCast(PermanentPredicate controllerControlsPredicate, List<CastingCost> additionalCosts) {
        this(controllerControlsPredicate, null, additionalCosts);
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        if (alternateManaCost == null) {
            return additionalCosts;
        }
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(new ManaCastingCost(alternateManaCost)),
                        additionalCosts.stream())
                .toList();
    }
}
