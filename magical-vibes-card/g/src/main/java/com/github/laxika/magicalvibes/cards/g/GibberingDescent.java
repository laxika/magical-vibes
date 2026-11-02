package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "66")
public class GibberingDescent extends Card {

    public GibberingDescent() {
        addCastingOption(new MadnessCast("{2}{B}{B}"));
        addStaticEffect(PlayersSkipUpkeepStepEffect.controllerWithEmptyHand());
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER),
                new DiscardEffect(1, DiscardRecipient.ACTIVE_PLAYER)));
    }
}
