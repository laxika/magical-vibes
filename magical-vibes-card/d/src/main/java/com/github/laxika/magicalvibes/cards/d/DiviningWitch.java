package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "57")
public class DiviningWitch extends Card {

    public DiviningWitch() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ChooseCardNameAtResolutionEffect(),
                        new ExileTopCardOfOwnLibraryEffect(false, Subjects.you(), new Fixed(6)),
                        new RevealCardsFromTopUntilEffect(named),
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay(named)),
                        ExileGraveyardCardsEffect.cards(Subjects.restRevealedThisWay(named))
                ),
                "{1}{B}, {T}, Discard a card: Choose a card name. Exile the top six cards of your library, then reveal cards from the top of your library until you reveal a card with the chosen name. Put that card into your hand and exile all other cards revealed this way."
        ));
    }
}
