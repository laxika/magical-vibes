package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "59")
public class IsolatedWatchtower extends Card {

    public IsolatedWatchtower() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ScryEffect(1),
                        new RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect(CardPredicateUtils.basicLand())
                ),
                "{2}, {T}: Scry 1, then you may reveal the top card of your library. If a basic land card is revealed this way, put it onto the battlefield tapped. Activate only if an opponent controls at least two more lands than you.",
                ActivationTimingRestriction.OPPONENT_CONTROLS_AT_LEAST_TWO_MORE_LANDS
        ));
    }
}
