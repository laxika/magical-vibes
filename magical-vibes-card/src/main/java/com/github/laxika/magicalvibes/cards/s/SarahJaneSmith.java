package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "6")
@CardRegistration(set = "WHO", collectorNumber = "347")
public class SarahJaneSmith extends Card {

    public SarahJaneSmith() {
        // Whenever you cast a historic spell, investigate. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(new SpellCastTriggerEffect(
                        new CardIsHistoricPredicate(),
                        List.of(CreateTokenEffect.ofClueToken(1)))));
    }
}
