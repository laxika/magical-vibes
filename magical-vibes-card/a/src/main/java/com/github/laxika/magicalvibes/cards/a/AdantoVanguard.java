package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "1")
public class AdantoVanguard extends Card {

    public AdantoVanguard() {
        // As long as Adanto Vanguard is attacking, it gets +2/+0.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 0, GrantScope.SELF, new PermanentIsAttackingPredicate()));

        // Pay 4 life: Adanto Vanguard gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(4),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)),
                "Pay 4 life: Adanto Vanguard gains indestructible until end of turn."));
    }
}
