package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INV", collectorNumber = "13")
public class DeathOrGlory extends Card {

    public DeathOrGlory() {
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(SeparateIntoPilesEffect.cards(
                Subjects.cardsInYourGraveyard(new CardTypePredicate(CardType.CREATURE)),
                Subjects.playersThisWay(StepKind.CHOOSE), "creature cards"));
        addSpellEffect(ExileGraveyardCardsEffect.cards(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.except(
                Subjects.cardsInYourGraveyard(new CardTypePredicate(CardType.CREATURE)),
                Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
