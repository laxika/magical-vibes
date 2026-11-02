package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "65")
@CardRegistration(set = "C20", collectorNumber = "115")
public class LunarMystic extends Card {

    public LunarMystic() {
        // Whenever you cast an instant spell, you may pay {1}. If you do, draw a card.
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.INSTANT),
                        List.of(new DrawCardEffect(1)),
                        "{1}"),
                "Pay {1} to draw a card?"));
    }
}
