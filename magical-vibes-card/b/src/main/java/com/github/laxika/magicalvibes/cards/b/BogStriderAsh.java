package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "198")
public class BogStriderAsh extends Card {

    public BogStriderAsh() {
        // Whenever a player casts a Goblin spell, you may pay {G}. If you do, you gain 2 life.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.GOBLIN),
                List.of(new MayPayManaEffect("{G}", new GainLifeEffect(2),
                        "Pay {G} to gain 2 life?"))));
    }
}
