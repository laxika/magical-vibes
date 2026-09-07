package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "24")
public class MinimusContainment extends Card {

    public MinimusContainment() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.STATIC,
                        new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.TREASURE, GrantScope.ENCHANTED_PERMANENT, true))
                .addEffect(EffectSlot.STATIC,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                        "{T}, Sacrifice this artifact: Add one mana of any color."
                                ),
                                GrantScope.ENCHANTED_PERMANENT
                        ));
    }
}
