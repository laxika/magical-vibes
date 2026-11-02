package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "157")
@CardRegistration(set = "SPM", collectorNumber = "276")
@CardRegistration(set = "OM1", collectorNumber = "153")
public class UltimateGreenGoblin extends Card {

    public UltimateGreenGoblin() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                CreateTokenEffect.ofTreasureToken(1)));
        addCastingOption(new GraveyardCast(null, "{2}{B/R}", List.of(), new CardDiscardedThisTurn()));
    }
}
