package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlotExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "218")
public class MakeYourOwnLuck extends Card {

    public MakeYourOwnLuck() {
        addSpellEffect(LookAtTopCardsEffect.mayExileOneMatchingRestToHand(
                3, new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addSpellEffect(new PlotExiledCardsEffect(Subjects.cardsThisWay(StepKind.EXILE)));
    }
}
