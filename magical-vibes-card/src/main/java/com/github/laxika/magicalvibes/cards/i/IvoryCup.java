package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "305")
public class IvoryCup extends Card {

    public IvoryCup() {
        // Whenever a player casts a white spell, you may pay {1}. If you do, you gain 1 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.WHITE),
                        List.of(new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?"))));
    }
}
