package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "198")
public class PolisCrusher extends Card {

    public PolisCrusher() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addEffect(EffectSlot.STATIC, new ProtectionFromCardTypesEffect(Set.of(CardType.ENCHANTMENT)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}{G}",
                List.of(new MonstrosityEffect(3)),
                "{4}{R}{G}: Monstrosity 3."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(
                monstrous,
                new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsEnchantmentPredicate(), 0)));
    }
}
