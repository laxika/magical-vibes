package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "BRO", collectorNumber = "163")
public class PhyrexianDragonEngine extends Card {

    public PhyrexianDragonEngine() {
        addEffect(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD, new MayEffect(
                SequenceEffect.of(new DiscardHandEffect(), new DrawCardEffect(3)),
                "Discard your hand?"
        ));

        addUnearth("{3}{R}{R}");
    }
}
