package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "215")
public class BorosReckoner extends Card {

    public BorosReckoner() {
        // Whenever this creature is dealt damage, it deals that much damage to any target.
        // The damage amount snapshots into xValue; the controller chooses the target when serviced.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DealDamageToAnyTargetEffect(new XValue()));

        // {R/W}: This creature gains first strike until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{R/W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{R/W}: This creature gains first strike until end of turn."));
    }
}
