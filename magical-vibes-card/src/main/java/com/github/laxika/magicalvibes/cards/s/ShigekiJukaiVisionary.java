package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "206")
public class ShigekiJukaiVisionary extends Card {

    public ShigekiJukaiVisionary() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(
                        new ReturnSelfToHandCost(),
                        new LookAtTopCardsEffect(
                                new Fixed(4),
                                new Fixed(1),
                                new CardTypePredicate(CardType.LAND),
                                LookDestination.GRAVEYARD,
                                true,
                                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                                true)
                ),
                "{1}{G}, {T}, Return Shigeki to its owner's hand: Reveal the top four cards of your library. "
                        + "You may put a land card from among them onto the battlefield tapped. Put the rest into your graveyard."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{X}{G}{G}",
                List.of(new ReturnTargetCardsFromGraveyardToHandEffect(
                        new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY)),
                        0,
                        true)),
                "Channel — {X}{X}{G}{G}, Discard this card: Return X target nonlegendary cards from your graveyard to your hand."
        ));
    }
}
