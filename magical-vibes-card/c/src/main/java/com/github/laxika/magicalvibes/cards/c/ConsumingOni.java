package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "17")
public class ConsumingOni extends Card {

    public ConsumingOni() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                ChooseCardEffect.atRandom(
                        Subjects.cardsInYourHand(new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                        "a nonland card in your hand"),
                new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                        Triggers.youCastThisSpell(), new LoseLifeEffect(3)));
    }
}
