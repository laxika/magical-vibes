package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnMatchingPermanentsUnlessOwnerPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Cut the Tethers — "For each Spirit, return it to its owner's hand unless that player pays {3}."
 *
 * <p>Every Spirit on the battlefield, whoever controls it, gets its own pay-or-be-bounced decision
 * made by its OWNER — "that player" refers back to the owner the sentence just named, so a Spirit
 * that has changed hands is kept or lost by the player whose hand it would return to.
 */
@CardRegistration(set = "CHK", collectorNumber = "56")
public class CutTheTethers extends Card {

    public CutTheTethers() {
        addEffect(EffectSlot.SPELL, new ReturnMatchingPermanentsUnlessOwnerPaysEffect(
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT), "{3}"));
    }
}
