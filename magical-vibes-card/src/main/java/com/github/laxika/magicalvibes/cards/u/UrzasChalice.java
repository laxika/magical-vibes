package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "75")
@CardRegistration(set = "ME1", collectorNumber = "171")
public class UrzasChalice extends Card {

    public UrzasChalice() {
        // Whenever a player casts an artifact spell, you may pay {1}. If you do, you gain 1 life.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?"))));
    }
}
