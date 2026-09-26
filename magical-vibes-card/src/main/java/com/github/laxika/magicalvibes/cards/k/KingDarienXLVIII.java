package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "204")
public class KingDarienXLVIII extends Card {

    public KingDarienXLVIII() {
        // Other creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));

        // {3}{G}{W}: Put a +1/+1 counter on King Darien XLVIII and create a 1/1 white Soldier creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{W}",
                List.of(
                        new PutCountersOnSourceEffect(1, 1, 1),
                        CreateTokenEffect.whiteSoldier(1)
                ),
                "{3}{G}{W}: Put a +1/+1 counter on King Darien XLVIII and create a 1/1 white Soldier creature token."
        ));

        // Sacrifice King Darien XLVIII: Creature tokens you control gain hexproof and indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE),
                                GrantScope.OWN_CREATURES,
                                new PermanentIsTokenPredicate())
                ),
                "Sacrifice King Darien XLVIII: Creature tokens you control gain hexproof and indestructible until end of turn."
        ));
    }
}
