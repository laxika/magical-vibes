package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "CON", collectorNumber = "77")
public class WorldheartPhoenix extends Card {

    public WorldheartPhoenix() {
        // "You may cast this card from your graveyard by paying {W}{U}{B}{R}{G} rather than paying
        // its mana cost."
        addCastingOption(new GraveyardCast("{W}{U}{B}{R}{G}"));
        // "If you do, it enters with two +1/+1 counters on it." — the graveyard cast is the only way
        // to cast it from the graveyard, so gate the counters on being cast from that zone.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
    }
}
