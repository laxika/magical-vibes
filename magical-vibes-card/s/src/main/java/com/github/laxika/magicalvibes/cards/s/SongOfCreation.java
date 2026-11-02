package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "210")
@CardRegistration(set = "SLD", collectorNumber = "1603")
public class SongOfCreation extends Card {

    public SongOfCreation() {
        // You may play an additional land on each of your turns.
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));

        // Whenever you cast a spell, draw two cards.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect(2))));

        // At the beginning of your end step, discard your hand.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DiscardHandEffect());
    }
}
