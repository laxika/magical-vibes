package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "3")
@CardRegistration(set = "40K", collectorNumber = "173")
@CardRegistration(set = "40K", collectorNumber = "179")
@CardRegistration(set = "40K", collectorNumber = "320")
public class InquisitorGreyfax extends Card {

    public InquisitorGreyfax() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        CreateTokenEffect.ofClueToken(1)
                ),
                "{1}, {T}: Tap target creature an opponent controls. Investigate.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
