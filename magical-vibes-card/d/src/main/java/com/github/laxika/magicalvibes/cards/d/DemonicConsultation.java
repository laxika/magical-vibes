package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

@CardRegistration(set = "ICE", collectorNumber = "121")
@CardRegistration(set = "ME2", collectorNumber = "85")
@CardRegistration(set = "SLD", collectorNumber = "1127")
@CardRegistration(set = "SLC", collectorNumber = "4")
@CardRegistration(set = "SLC", collectorNumber = "31")
@CardRegistration(set = "MB2", collectorNumber = "181")
public class DemonicConsultation extends Card {

    public DemonicConsultation() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        // Choose a card name. Exile the top six cards of your library, then reveal cards from the
        // top of your library until you reveal a card with the chosen name. Put that card into your
        // hand and exile all other cards revealed this way.
        addSpellEffect(new ChooseCardNameAtResolutionEffect());
        addSpellEffect(new ExileTopCardOfOwnLibraryEffect(false, Subjects.you(), new Fixed(6)));
        addSpellEffect(new RevealCardsFromTopUntilEffect(named));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.revealedThisWay(named)));
        addSpellEffect(ExileGraveyardCardsEffect.cards(Subjects.restRevealedThisWay(named)));
    }
}
