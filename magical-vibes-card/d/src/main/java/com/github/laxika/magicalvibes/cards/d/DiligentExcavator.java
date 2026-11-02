package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "51")
public class DiligentExcavator extends Card {

    public DiligentExcavator() {
        // Whenever you cast a historic spell, target player mills two cards.
        // (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardIsHistoricPredicate(),
                List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER))
        ));
    }
}
