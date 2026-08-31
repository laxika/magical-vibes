package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "33")
public class Cloudseeder extends Card {

    public Cloudseeder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                1,
                                "Cloud Sprite",
                                1,
                                1,
                                CardColor.BLUE,
                                List.of(CardSubtype.FAERIE),
                                Set.of(Keyword.FLYING),
                                Set.of(),
                                Map.of(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                                        "creatures with flying")))
                ),
                "{U}, {T}, Discard a card: Create a 1/1 blue Faerie creature token named Cloud Sprite. It has flying and \"This token can block only creatures with flying.\""
        ));
    }
}
