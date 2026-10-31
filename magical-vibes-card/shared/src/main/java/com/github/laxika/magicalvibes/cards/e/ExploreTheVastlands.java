package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;

public class ExploreTheVastlands extends Card {

    public ExploreTheVastlands() {
        addSpellEffect(LookAtTopCardsRevealTwoTypesToHandThenRestEffect.landAndInstantOrSorceryToHandRestOnBottomRandom(5));
        addSpellEffect(new GainLifeEffect(new Fixed(3), GainLifeRecipient.EACH_PLAYER));
    }
}
