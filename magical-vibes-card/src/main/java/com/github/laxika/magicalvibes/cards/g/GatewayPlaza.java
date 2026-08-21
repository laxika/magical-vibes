package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "247")
@CardRegistration(set = "RNA", collectorNumber = "247")
public class GatewayPlaza extends Card {

    public GatewayPlaza() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{1}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
