package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;

public class ExploreTheVastlands extends Card {

    public ExploreTheVastlands() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect.landAndInstantOrSorceryToHandRestOnBottomRandom(5));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Fixed(3), GainLifeRecipient.EACH_PLAYER));
    }
}
