package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "74")
@CardRegistration(set = "NCC", collectorNumber = "174")
@CardRegistration(set = "DSC", collectorNumber = "224")
@CardRegistration(set = "OTC", collectorNumber = "233")
public class LifeInsurance extends Card {

    public LifeInsurance() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                UnlessPaysEffect.youMayPay("{W/B}", new LoseLifeEffect(1, Subjects.eachOpponent(), true))
                        .withPrompt("Pay {W/B} to extort?"));

        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANY),
                SequenceEffect.of(
                        new LoseLifeEffect(1, Subjects.you()),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
