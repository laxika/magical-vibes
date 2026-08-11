package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "92")
public class BoggartMischief extends Card {

    public BoggartMischief() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new BlightEffect(1, new CreateTokenEffect(
                        2, "Goblin", 1, 1, CardColor.BLACK,
                        Set.of(CardColor.BLACK, CardColor.RED), List.of(CardSubtype.GOBLIN))),
                "Blight 1?"));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.GOBLIN),
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1))));
    }
}
