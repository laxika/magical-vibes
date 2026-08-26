package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.ArchiveHaunt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MID", collectorNumber = "68")
public class OverwhelmedArchivist extends Card {

    public OverwhelmedArchivist() {
        setBackFaceCard(new ArchiveHaunt());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, loot());
        addCastingOption(new DisturbCast("{3}{U}"));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "ArchiveHaunt";
    }

    private static SequenceEffect loot() {
        return SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
