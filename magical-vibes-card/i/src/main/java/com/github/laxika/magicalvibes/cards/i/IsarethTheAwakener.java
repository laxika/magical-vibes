package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreatureWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Isareth the Awakener — {1}{B}{B} Legendary Creature — Human Wizard (3/3).
 *
 * <p>Deathtouch (from Scryfall). "Whenever Isareth the Awakener attacks, you may pay {X}. When you
 * do, return target creature card with mana value X from your graveyard to the battlefield with a
 * corpse counter on it. If that creature would leave the battlefield, exile it instead of putting
 * it anywhere else."</p>
 */
@CardRegistration(set = "M19", collectorNumber = "104")
@CardRegistration(set = "CMM", collectorNumber = "168")
public class IsarethTheAwakener extends Card {

    public IsarethTheAwakener() {
        addTrigger(Triggers.attacks(),
                new PayXManaReanimateCreatureWithManaValueXEffect(CounterType.CORPSE, true));
    }
}
