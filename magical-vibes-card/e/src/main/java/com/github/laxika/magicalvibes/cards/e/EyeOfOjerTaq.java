package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.ApexObservatory;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "15")
@CardRegistration(set = "LCC", collectorNumber = "35")
public class EyeOfOjerTaq extends Card {

    public EyeOfOjerTaq() {
        setBackFaceCard(new ApexObservatory());

        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(
                        new ExileSelfCost(),
                        CraftMaterialCost.twoSharingCardType(),
                        new ReturnSourceFromExileTransformedEffect()),
                "Craft with two that share a card type {6} ({6}, Exile this artifact, Exile the two from among "
                        + "other permanents you control and/or cards from your graveyard: Return this card "
                        + "transformed under its owner's control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "ApexObservatory";
    }
}
