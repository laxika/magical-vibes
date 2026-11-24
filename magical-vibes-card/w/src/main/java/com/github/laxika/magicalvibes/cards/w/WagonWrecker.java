package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YOTJ", collectorNumber = "15")
public class WagonWrecker extends Card {

    public WagonWrecker() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        ChooseCardEffect.atRandom(Subjects.cardsInHands(Subjects.triggeringPlayer(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))), "a nonland card"),
                        new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                                Triggers.youCastThisSpell(), new LoseLifeEffect(2))));
    }
}
