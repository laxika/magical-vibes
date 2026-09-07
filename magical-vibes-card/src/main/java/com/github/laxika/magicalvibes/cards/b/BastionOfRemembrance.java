package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "73")
public class BastionOfRemembrance extends Card {

    public BastionOfRemembrance() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new GainLifeEffect(1));
    }
}
