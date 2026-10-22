package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "CON", collectorNumber = "77")
@CardRegistration(set = "MM2", collectorNumber = "135")
public class WorldheartPhoenix extends Card {

    public WorldheartPhoenix() {
        // "You may cast this card from your graveyard by paying {W}{U}{B}{R}{G} rather than paying
        // its mana cost."
        addCastingOption(new GraveyardCast("{W}{U}{B}{R}{G}"));
        // "If you do, it enters with two +1/+1 counters on it." Casting it at its mana cost via another
        // graveyard permission (Havengul Lich) doesn't pay the graveyard cost, so gate on the alternate cost.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new CastForAlternateCost(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
    }
}
