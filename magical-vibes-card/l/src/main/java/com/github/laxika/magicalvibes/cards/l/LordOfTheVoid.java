package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RVR", collectorNumber = "81")
public class LordOfTheVoid extends Card {

    public LordOfTheVoid() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ExileTopCardOfOwnLibraryEffect(false, Subjects.triggeringPlayer(), new Fixed(7)),
                        ChooseCardEffect.oneOf(
                                Subjects.cardsThisWay(StepKind.EXILE, new CardTypePredicate(CardType.CREATURE)),
                                "a creature card to put onto the battlefield"),
                        ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(
                                Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
