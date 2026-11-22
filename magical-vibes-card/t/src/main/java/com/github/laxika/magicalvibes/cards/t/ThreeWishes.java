package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "45")
public class ThreeWishes extends Card {

    public ThreeWishes() {
        // Exile the top three cards of your library face down. You may look at those cards for as
        // long as they remain exiled. Until your next turn, you may play those cards. At the
        // beginning of your next upkeep, put any of those cards you didn't play into your graveyard.
        // Play window closes as your next turn begins; the cleanup is one trigger at your next upkeep.
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(3, false)
                .exiledFaceDown()
                .until(Duration.UNTIL_YOUR_NEXT_TURN));
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.upkeep(ActivePlayer.YOU),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
