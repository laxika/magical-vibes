package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;

@CardRegistration(set = "YDMU", collectorNumber = "16")
public class MarwynsKindred extends Card {

    public MarwynsKindred() {
        addEffect(EffectSlot.SPELL, new CreateTokenCopyOfCardEffect(
                new MarwynTheNurturer(), new CreateTokenCopyOfTargetPermanentEffect()));
        addEffect(EffectSlot.SPELL, new CreateTokenCopyOfCardEffect(
                new LlanowarElves(), new CreateTokenCopyOfTargetPermanentEffect(new XValue())));
    }
}
