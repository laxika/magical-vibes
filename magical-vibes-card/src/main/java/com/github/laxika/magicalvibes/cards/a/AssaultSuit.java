package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;

@CardRegistration(set = "CMM", collectorNumber = "369")
@CardRegistration(set = "C14", collectorNumber = "53")
public class AssaultSuit extends Card {

    public AssaultSuit() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackControllerEffect());
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeSacrificedEffect(), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new TargetPlayerGainsControlOfEnchantedPermanentEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "Have that player gain control of equipped creature until end of turn?"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
