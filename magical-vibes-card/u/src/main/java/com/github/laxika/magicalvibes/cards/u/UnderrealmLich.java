package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UnderrealmLichDrawReplacementEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "211")
public class UnderrealmLich extends Card {

    public UnderrealmLich() {
        addEffect(EffectSlot.STATIC, new UnderrealmLichDrawReplacementEffect());

        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(
                        new PayLifeCost(4),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)
                ),
                "Pay 4 life: This creature gains indestructible until end of turn. Tap it."));
    }
}
