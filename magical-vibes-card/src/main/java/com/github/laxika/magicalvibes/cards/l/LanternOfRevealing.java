package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "259")
public class LanternOfRevealing extends Card {

    public LanternOfRevealing() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(
                        new CardTypePredicate(CardType.LAND), true)),
                "{4}, {T}: Look at the top card of your library. If it's a land card, you may put it "
                        + "onto the battlefield tapped. If you don't put the card onto the battlefield, "
                        + "you may put it on the bottom of your library."
        ));
    }
}
