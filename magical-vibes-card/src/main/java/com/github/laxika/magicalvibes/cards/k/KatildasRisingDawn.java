package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

public class KatildasRisingDawn extends Card {

    public KatildasRisingDawn() {
        PermanentCount spiritsOrEnchantments = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                        new PermanentIsEnchantmentPredicate())),
                CountScope.CONTROLLER);
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                        Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        new ProtectionFromSubtypesEffect(Set.of(CardSubtype.VAMPIRE)),
                        GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        spiritsOrEnchantments, spiritsOrEnchantments, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
