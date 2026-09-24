package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "191")
public class CombineChrysalis extends Card {

    public CombineChrysalis() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.OWN_CREATURES, new PermanentIsTokenPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}{U}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsTokenPredicate(), "a token"),
                        new CreateTokenEffect("Beast", 4, 4, CardColor.GREEN,
                                List.of(CardSubtype.BEAST), Set.of(), Set.of())
                ),
                "{2}{G}{U}, {T}, Sacrifice a token: Create a 4/4 green Beast creature token. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
