package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "6")
@CardRegistration(set = "WHO", collectorNumber = "611")
@CardRegistration(set = "WHO", collectorNumber = "347")
@CardRegistration(set = "WHO", collectorNumber = "536")
@CardRegistration(set = "WHO", collectorNumber = "938")
@CardRegistration(set = "WHO", collectorNumber = "1127")
public class SarahJaneSmith extends Card {

    public SarahJaneSmith() {
        // Whenever you cast a historic spell, investigate. This ability triggers only once each turn.
        addTrigger(Triggers.youCastSpell(),
                new OncePerTurnTriggerEffect(new SpellCastTriggerEffect(
                        new CardIsHistoricPredicate(),
                        List.of(CreateTokenEffect.ofClueToken(1)))));
    }
}
