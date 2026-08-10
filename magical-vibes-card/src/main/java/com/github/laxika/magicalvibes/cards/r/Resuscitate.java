package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "121")
public class Resuscitate extends Card {

    public Resuscitate() {
        addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{1}",
                        List.of(new RegenerateEffect()),
                        "{1}: Regenerate this creature."
                ),
                GrantScope.OWN_CREATURES,
                null,
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
