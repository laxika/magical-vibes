package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.TargetSpellWouldDestroyLandYouControl;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "13")
public class Equinox extends Card {

    public Equinox() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(true, null, List.of(
                                new ConditionalEffect(
                                        new TargetSpellWouldDestroyLandYouControl(),
                                        new CounterSpellEffect())),
                                "{T}: Counter target spell if it would destroy a land you control."),
                        GrantScope.ENCHANTED_PERMANENT));
    }
}
