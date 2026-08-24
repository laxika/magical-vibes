package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "136")
public class OutlawsFury extends Card {

    private static final Set<CardSubtype> OUTLAW_SUBTYPES = Set.of(
            CardSubtype.ASSASSIN,
            CardSubtype.MERCENARY,
            CardSubtype.PIRATE,
            CardSubtype.ROGUE,
            CardSubtype.WARLOCK);

    public OutlawsFury() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 0));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasAnySubtypePredicate(OUTLAW_SUBTYPES)),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1)));
    }
}
