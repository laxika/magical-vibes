package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "137")
public class DeathPulse extends Card {

    public DeathPulse() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, -4));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{B}{B}",
                List.of(new BoostTargetCreatureEffect(-1, -1), new DrawCardEffect(1)),
                "Cycling {1}{B}{B} ({1}{B}{B}, Discard this card: Draw a card.)",
                null, null, null, null, List.of(), 0, 1));
    }
}
