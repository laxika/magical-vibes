package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "110")
public class LordOfTheForsaken extends Card {

    public LordOfTheForsaken() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "another creature"),
                        new MillEffect(3, MillRecipient.TARGET_PLAYER)),
                "{B}, Sacrifice another creature: Target player mills three cards."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(1), new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 1, new ManaRestriction.GraveyardSpells())),
                "Pay 1 life: Add {C}. Spend this mana only to cast a spell from your graveyard."
        ));
    }
}
