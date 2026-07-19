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
 */
public record GraveyardCast(PermanentPredicate controllerControlsPredicate, String alternateManaCost) implements CastingOption {

    public GraveyardCast() {
        this(null, null);
    }

    public GraveyardCast(PermanentPredicate controllerControlsPredicate) {
        this(controllerControlsPredicate, null);
    }

    public GraveyardCast(String alternateManaCost) {
        this(null, alternateManaCost);
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return alternateManaCost == null ? List.of() : List.of(new ManaCastingCost(alternateManaCost));
    }
}
