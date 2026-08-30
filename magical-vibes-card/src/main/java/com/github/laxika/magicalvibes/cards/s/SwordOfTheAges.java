package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSacrificedCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "296")
public class SwordOfTheAges extends Card {

    public SwordOfTheAges() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SacrificeAnyNumberOfPermanentsCost(
                                new PermanentIsCreaturePredicate(), true, true),
                        new DealDamageToAnyTargetEffect(new XValue()),
                        new ExileSourceCardFromGraveyardEffect(),
                        new ExileSacrificedCardsFromGraveyardEffect()),
                "{T}, Sacrifice this artifact and any number of creatures you control: This artifact deals X damage to any target, where X is the total power of the creatures sacrificed this way, then exile this artifact and those creature cards."));
    }
}
