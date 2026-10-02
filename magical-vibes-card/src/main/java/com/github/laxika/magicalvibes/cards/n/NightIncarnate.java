package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;

import java.util.List;

@CardRegistration(set = "C18", collectorNumber = "17")
public class NightIncarnate extends Card {

    public NightIncarnate() {
        // When this creature leaves the battlefield, all creatures get -3/-3 until end of turn.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new BoostAllCreaturesEffect(-3, -3));

        // Evoke {3}{B}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{B}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
