package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "266")
@CardRegistration(set = "LCI", collectorNumber = "313")
public class ThroneOfTheGrimCaptain extends Card {

    public ThroneOfTheGrimCaptain() {
        setBackFaceCard(new TheGrimCaptain());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(2, MillRecipient.CONTROLLER)),
                "{T}: Mill two cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new ExileSelfCost(),
                        CraftMaterialCost.withRequiredSubtypes(
                                CardSubtype.DINOSAUR, CardSubtype.MERFOLK,
                                CardSubtype.PIRATE, CardSubtype.VAMPIRE),
                        new ReturnSourceFromExileTransformedEffect()
                ),
                "Craft with a Dinosaur, a Merfolk, a Pirate, and a Vampire {4}.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheGrimCaptain";
    }
}
