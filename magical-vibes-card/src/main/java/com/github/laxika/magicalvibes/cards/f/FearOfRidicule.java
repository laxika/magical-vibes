package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "7")
public class FearOfRidicule extends Card {

    public FearOfRidicule() {
        PermanentPredicate enchantmentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentIsCreaturePredicate()
        ));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.ALL_OWN_CREATURES, enchantmentCreature));

        CreateTokenCopyOfTargetPermanentEffect tokenCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(CardType.ENCHANTMENT), 1, 1, java.util.Map.of(),
                false, false, false, false, false, false, null, Set.of());
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        enchantmentCreature,
                        new ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect(tokenCopy),
                        false,
                        true));
    }
}
