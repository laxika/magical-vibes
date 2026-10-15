package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TLA", collectorNumber = "67")
public class OtterPenguin extends Card {

    public OtterPenguin() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                SequenceEffect.of(
                        new BoostSelfEffect(1, 2),
                        new MakeCreatureUnblockableEffect(true)));
    }
}
