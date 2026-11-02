package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "44")
public class ArtificersAssistant extends Card {

    public ArtificersAssistant() {
        // Whenever you cast a historic spell, scry 1.
        // (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardIsHistoricPredicate(),
                List.of(new ScryEffect(1))
        ));
    }
}
