package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "149")
public class ScryingSheets extends Card {

    public ScryingSheets() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{S}",
                List.of(new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardSupertypePredicate(CardSupertype.SNOW), false)),
                "{1}{S}, {T}: Look at the top card of your library. If that card is snow, you may reveal it and put it into your hand."
        ));
    }
}
