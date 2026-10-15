package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "146")
public class ViashinoLashclaw extends Card {

    public ViashinoLashclaw() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)
                ),
                "{T}, Discard a card: Creatures you control gain haste until end of turn."
        ));
    }
}
