package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

public class KyrenFlamewright extends Card {

    public KyrenFlamewright() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(2, "Elemental", 1, 1, CardColor.BLUE,
                                Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)),
                        new BoostAllOwnCreaturesEffect(1, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)),
                "{2}{R}, {T}, Discard a card: Create two 1/1 blue and red Elemental creature tokens. "
                        + "Creatures you control get +1/+0 and gain haste until end of turn."));
    }
}
