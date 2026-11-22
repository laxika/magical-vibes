package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

@CardRegistration(set = "INV", collectorNumber = "100")
public class DesperateResearch extends Card {

    public DesperateResearch() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addSpellEffect(ChooseCardNameAtResolutionEffect.otherThanABasicLand());
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(7)));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.revealedThisWay(named)));
        addSpellEffect(ExileGraveyardCardsEffect.cards(Subjects.restRevealedThisWay(named)));
    }
}
