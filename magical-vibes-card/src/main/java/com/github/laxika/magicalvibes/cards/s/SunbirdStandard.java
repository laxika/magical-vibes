package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "262")
public class SunbirdStandard extends Card {

    public SunbirdStandard() {
        setBackFaceCard(new SunbirdEffigy());

        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(1, null, false, false),
                        new ReturnSourceFromExileTransformedEffect()),
                "{5}, Exile this artifact, Exile one or more other permanents you control and/or cards from your graveyard: "
                        + "Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "SunbirdEffigy";
    }
}
