package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "92")
public class BoggartMischief extends Card {

    public BoggartMischief() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new BlightEffect(1, new CreateTokenEffect(
                        2, "Goblin", 1, 1, CardColor.BLACK,
                        Set.of(CardColor.BLACK, CardColor.RED), List.of(CardSubtype.GOBLIN))),
                "Blight 1?"));

        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT), new GainLifeEffect(1)));
    }
}
