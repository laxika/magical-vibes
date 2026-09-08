package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "60")
public class CoffinPuppets extends Card {

    public CoffinPuppets() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate()),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)
                ),
                "Sacrifice two lands: Return this card from your graveyard to the battlefield. "
                        + "Activate only during your upkeep and only if you control a Swamp.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withActivationCondition(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                "You must control a Swamp"
        ));
    }
}
