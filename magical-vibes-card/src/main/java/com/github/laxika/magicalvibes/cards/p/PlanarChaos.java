package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "97")
public class PlanarChaos extends Card {

    public PlanarChaos() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new FlipCoinWinEffect(null, new SacrificeSelfEffect()));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(new FlipCoinWinEffect(null, new CounterSpellEffect()))));
    }
}
