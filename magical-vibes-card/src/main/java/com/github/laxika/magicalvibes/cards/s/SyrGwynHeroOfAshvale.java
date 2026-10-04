package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

@CardRegistration(set = "ELD", collectorNumber = "330")
public class SyrGwynHeroOfAshvale extends Card {

    public SyrGwynHeroOfAshvale() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsEquippedPredicate(),
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new EquipActivatedAbility("{0}",
                        new PermanentHasSubtypePredicate(CardSubtype.KNIGHT),
                        "Target must be a Knight you control"),
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)));
    }
}
