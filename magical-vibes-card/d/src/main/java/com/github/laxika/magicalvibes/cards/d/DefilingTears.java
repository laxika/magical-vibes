package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "99")
public class DefilingTears extends Card {

    public DefilingTears() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.BLACK))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, -1))
                .addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                                "{B}: Regenerate this creature."),
                        GrantScope.TARGET,
                        null,
                        EffectDuration.UNTIL_END_OF_TURN
                ));
    }
}
