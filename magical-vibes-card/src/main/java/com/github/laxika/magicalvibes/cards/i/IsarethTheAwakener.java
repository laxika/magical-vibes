package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreatureWithManaValueXEffect;

/**
 * Isareth the Awakener — {1}{B}{B} Legendary Creature — Human Wizard (3/3).
 *
 * <p>Deathtouch (from Scryfall). "Whenever Isareth the Awakener attacks, you may pay {X}. When you
 * do, return target creature card with mana value X from your graveyard to the battlefield with a
 * corpse counter on it. If that creature would leave the battlefield, exile it instead of putting
 * it anywhere else."</p>
 */
@CardRegistration(set = "M19", collectorNumber = "104")
public class IsarethTheAwakener extends Card {

    public IsarethTheAwakener() {
        addEffect(EffectSlot.ON_ATTACK,
                new PayXManaReanimateCreatureWithManaValueXEffect(CounterType.CORPSE, true));
    }
}
