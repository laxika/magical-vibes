package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "256")
@CardRegistration(set = "RVR", collectorNumber = "252")
public class BottledCloister extends Card {

    public BottledCloister() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                ExileTargetPlayerHandEffect.handsOfWithSource(Subjects.you()).exiledFaceDown());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect(true),
                new DrawCardEffect()));
    }
}
