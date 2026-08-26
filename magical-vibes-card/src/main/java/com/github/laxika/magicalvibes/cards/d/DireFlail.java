package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "145")
public class DireFlail extends Card {

    public DireFlail() {
        setBackFaceCard(new DireBlunderbuss());

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(), new ReturnSourceFromExileTransformedEffect()),
                "Craft with artifact {3}{R}{R} ({3}{R}{R}, Exile this artifact, Exile another artifact you control "
                        + "or an artifact card from your graveyard: Return this card transformed under its owner's "
                        + "control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "DireBlunderbuss";
    }
}
