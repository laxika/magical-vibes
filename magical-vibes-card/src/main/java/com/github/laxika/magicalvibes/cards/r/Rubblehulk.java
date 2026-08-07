package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "191")
public class Rubblehulk extends Card {

    public Rubblehulk() {
        // Rubblehulk's power and toughness are each equal to the number of lands you control.
        final PermanentCount lands = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(lands, lands));

        // Bloodrush — {1}{R}{G}, Discard this card: Target attacking creature gets +X/+X
        // until end of turn, where X is the number of lands you control. Discard cost is
        // intrinsic to the hand ability; the engine pays it.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}{G}",
                List.of(new BoostTargetCreatureEffect(lands, lands)),
                "Bloodrush — {1}{R}{G}, Discard this card: Target attacking creature gets +X/+X until end of turn, where X is the number of lands you control.",
                TargetFilters.attackingCreature()));
    }
}
