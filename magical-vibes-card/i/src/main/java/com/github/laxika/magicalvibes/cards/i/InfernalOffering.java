package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C14", collectorNumber = "24")
@CardRegistration(set = "C21", collectorNumber = "146")
public class InfernalOffering extends Card {

    public InfernalOffering() {
        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                Subjects.allOf(Subjects.you(), Subjects.playersThisWay(StepKind.CHOOSE))));
        addSpellEffect(new DrawCardEffect(2, Subjects.playersThisWay(StepKind.SACRIFICE)));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.mandatory(1, creatureCard)
                .by(Subjects.you()));
        addSpellEffect(EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.mandatory(1, creatureCard)
                .by(Subjects.playersThisWay(StepKind.CHOOSE)));
    }
}
