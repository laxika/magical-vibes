package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

@CardRegistration(set = "MRD", collectorNumber = "78")
@CardRegistration(set = "UMA", collectorNumber = "116")
public class SpoilsOfTheVault extends Card {

    public SpoilsOfTheVault() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addSpellEffect(new ChooseCardNameAtResolutionEffect());
        addSpellEffect(new RevealCardsFromTopUntilEffect(named));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.revealedThisWay(named)));
        addSpellEffect(ExileGraveyardCardsEffect.cards(Subjects.restRevealedThisWay(named)));
        addSpellEffect(new LoseLifeEffect(new AmountThisWay(StepKind.EXILE), Subjects.you()));
    }
}
