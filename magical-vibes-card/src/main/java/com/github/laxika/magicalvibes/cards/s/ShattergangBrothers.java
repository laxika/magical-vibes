package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1315")
public class ShattergangBrothers extends Card {

    public ShattergangBrothers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificeCreatureCost(),
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.EACH_OPPONENT)),
                "{2}{B}, Sacrifice a creature: Each other player sacrifices a creature."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(),
                                SacrificeRecipient.EACH_OPPONENT)),
                "{2}{R}, Sacrifice an artifact: Each other player sacrifices an artifact."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsEnchantmentPredicate(), "an enchantment", false),
                        new SacrificePermanentsEffect(1, new PermanentIsEnchantmentPredicate(),
                                SacrificeRecipient.EACH_OPPONENT)),
                "{2}{G}, Sacrifice an enchantment: Each other player sacrifices an enchantment."
        ));
    }
}
