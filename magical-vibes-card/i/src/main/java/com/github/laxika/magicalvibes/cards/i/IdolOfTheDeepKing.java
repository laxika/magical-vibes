package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SovereignsMacuahuitl;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "155")
public class IdolOfTheDeepKing extends Card {

    public IdolOfTheDeepKing() {
        setBackFaceCard(new SovereignsMacuahuitl());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(), new ReturnSourceFromExileTransformedEffect()),
                "{2}{R}, Exile this artifact, Exile another artifact you control or an artifact card from your graveyard: "
                        + "Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "SovereignsMacuahuitl";
    }
}
