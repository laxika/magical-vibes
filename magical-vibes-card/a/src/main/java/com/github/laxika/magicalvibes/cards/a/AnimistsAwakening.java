package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "169")
@CardRegistration(set = "SOC", collectorNumber = "261")
@CardRegistration(set = "C20", collectorNumber = "166")
public class AnimistsAwakening extends Card {

    public AnimistsAwakening() {
        var landCard = new CardTypePredicate(CardType.LAND);
        // Reveal the top X cards of your library. Put all land cards from among them onto the
        // battlefield tapped and the rest on the bottom of your library in a random order.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // untap those lands.
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new XValue()));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(landCard)).enteringTapped());
        addSpellEffect(PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.restRevealedThisWay(landCard)));
        addSpellEffect(new ConditionalEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                        new UntapPermanentsEffect(Subjects.returnedThisResolution())));
    }
}
