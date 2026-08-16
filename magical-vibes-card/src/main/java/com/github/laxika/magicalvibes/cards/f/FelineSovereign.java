package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "180")
public class FelineSovereign extends Card {

    public FelineSovereign() {
        PermanentHasSubtypePredicate cats = new PermanentHasSubtypePredicate(CardSubtype.CAT);

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, cats));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.DOG)), GrantScope.OWN_CREATURES, cats));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        cats,
                        new DestroyPermanentDamagedPlayerControlsEffect(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsEnchantmentPredicate())), 0),
                        false,
                        true));
    }
}
