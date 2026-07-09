package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "198")
public class BogStriderAsh extends Card {

    public BogStriderAsh() {
        // Whenever a player casts a Goblin spell, you may pay {G}. If you do, you gain 2 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.GOBLIN),
                        List.of(new GainLifeEffect(2)),
                        "{G}"),
                "Pay {G} to gain 2 life?"
        ));
    }
}
