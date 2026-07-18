package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "359")
@CardRegistration(set = "5ED", collectorNumber = "409")
@CardRegistration(set = "8ED", collectorNumber = "321")
@CardRegistration(set = "7ED", collectorNumber = "324")
@CardRegistration(set = "6ED", collectorNumber = "318")
public class WoodenSphere extends Card {

    public WoodenSphere() {
        // Whenever a player casts a green spell, you may pay {1}. If you do, you gain 1 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN),
                        List.of(new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?"))));
    }
}
