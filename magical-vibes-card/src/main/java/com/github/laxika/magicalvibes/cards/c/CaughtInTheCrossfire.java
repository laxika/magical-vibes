package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "117")
public class CaughtInTheCrossfire extends Card {

    private static final Set<CardSubtype> OUTLAW_SUBTYPES = Set.of(
            CardSubtype.ASSASSIN,
            CardSubtype.MERCENARY,
            CardSubtype.PIRATE,
            CardSubtype.ROGUE,
            CardSubtype.WARLOCK);

    public CaughtInTheCrossfire() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{1}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Caught in the Crossfire deals 2 damage to each outlaw creature",
                        new DealDamageToEachMatchingPermanentEffect(2,
                                new PermanentHasAnySubtypePredicate(OUTLAW_SUBTYPES),
                                EachPermanentScope.ALL_PLAYERS)),
                new ChooseOneEffect.ChooseOneOption(
                        "Caught in the Crossfire deals 2 damage to each non-outlaw creature",
                        new DealDamageToEachMatchingPermanentEffect(2,
                                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(OUTLAW_SUBTYPES)),
                                EachPermanentScope.ALL_PLAYERS))
        )));
    }
}
