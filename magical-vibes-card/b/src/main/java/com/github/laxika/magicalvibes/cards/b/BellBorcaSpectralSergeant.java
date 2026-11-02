package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueNotedForSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NoteManaValueOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMC", collectorNumber = "145")
public class BellBorcaSpectralSergeant extends Card {

    public BellBorcaSpectralSergeant() {
        addStaticEffect(new NoteManaValueOfExiledCardsEffect());
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new GreatestManaValueNotedForSourceThisTurn(), new Fixed(5)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
