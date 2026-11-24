package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "28")
public class SkullpiercerGnat extends Card {

    public SkullpiercerGnat() {
        addStaticEffect(new ToxicEffect(1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        ChooseCardEffect.atRandom(Subjects.cardsInHands(Subjects.triggeringPlayer(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))), "a nonland card"),
                        new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                                Triggers.youCastThisSpell(), new GivePoisonCountersEffect(1, Subjects.you()))));
    }
}
