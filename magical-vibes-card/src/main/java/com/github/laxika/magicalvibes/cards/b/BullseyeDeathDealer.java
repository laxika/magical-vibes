package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "209")
public class BullseyeDeathDealer extends Card {

    public BullseyeDeathDealer() {
        CardNotPredicate nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Sacrifice an artifact",
                                new SacrificePermanentThenEffect(
                                        new PermanentIsArtifactPredicate(),
                                        new DealDamageToAnyTargetEffect(2),
                                        "an artifact")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Discard a nonland card",
                                new DiscardCardThenEffect(
                                        nonlandCard,
                                        new DealDamageToAnyTargetEffect(2),
                                        "a nonland card"))
                )),
                "Sacrifice an artifact or discard a nonland card to deal 2 damage?"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{3}, {T}, Sacrifice an artifact: Bullseye deals 2 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new DiscardCardTypeCost(nonlandCard, "nonland"),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{3}, {T}, Discard a nonland card: Bullseye deals 2 damage to any target."
        ));
    }
}
