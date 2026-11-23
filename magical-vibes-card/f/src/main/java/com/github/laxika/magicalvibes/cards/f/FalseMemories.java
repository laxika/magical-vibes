package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "37")
public class FalseMemories extends Card {

    public FalseMemories() {
        addSpellEffect(new MillEffect(7, Subjects.you()));
        addSpellEffect(RegisterDelayedStepTriggerEffect.atNext(
                Triggers.endStep(ActivePlayer.ANY), new ExileGraveyardCardsEffect(7, GraveyardExileScope.OWN)));
    }
}
