package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerLifeToSpecificValueEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "9ED", collectorNumber = "187")
public class FormOfTheDragon extends Card {

    public FormOfTheDragon() {
        // At the beginning of your upkeep, this enchantment deals 5 damage to any target.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToAnyTargetEffect(5));

        // At the beginning of each end step, your life total becomes 5.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SetControllerLifeToSpecificValueEffect(5));

        // Creatures without flying can't attack you.
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
