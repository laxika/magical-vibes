package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "228")
public class ZurEternalSchemer extends Card {

    public ZurEternalSchemer() {
        PermanentPredicate enchantmentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentIsCreaturePredicate()
        ));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK, Keyword.HEXPROOF),
                GrantScope.ALL_OWN_CREATURES,
                enchantmentCreature));

        PermanentPredicate nonAuraEnchantment = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA))
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new AnimatePermanentsEffect(
                        new TargetManaValue(), new TargetManaValue(),
                        List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.PERMANENT, null
                )),
                "{1}{W}: Target non-Aura enchantment you control becomes a creature in addition to its other types and has base power and base toughness each equal to its mana value.",
                new ControlledPermanentPredicateTargetFilter(
                        nonAuraEnchantment,
                        "Target must be a non-Aura enchantment you control"
                )
        ));
    }
}
