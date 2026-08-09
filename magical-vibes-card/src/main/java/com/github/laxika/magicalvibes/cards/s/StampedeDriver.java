package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "122")
public class StampedeDriver extends Card {

    public StampedeDriver() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                ),
                "{1}{G}, {T}, Discard a card: Creatures you control get +1/+1 and gain trample until end of turn."
        ));
    }
}
