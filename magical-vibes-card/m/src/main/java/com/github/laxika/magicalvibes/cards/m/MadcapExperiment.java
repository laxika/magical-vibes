package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "KLD", collectorNumber = "122")
public class MadcapExperiment extends Card {

    public MadcapExperiment() {
        // Reveal cards from the top of your library until you reveal an artifact card. Put that card onto the
        // battlefield and the rest on the bottom of your library in a random order. Madcap Experiment deals damage
        // to you equal to the number of cards revealed this way.
        var artifactCard = new CardTypePredicate(CardType.ARTIFACT);
        addSpellEffect(new RevealCardsFromTopUntilEffect(artifactCard));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(artifactCard)));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.restRevealedThisWay(artifactCard)));
        addSpellEffect(DealDamageEffect.to(Subjects.you(), new AmountThisWay(StepKind.REVEAL)));
    }
}
