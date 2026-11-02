package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "240")
public class WinterMisanthropicGuide extends Card {

    public WinterMisanthropicGuide() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new EachPlayerDrawsCardEffect(2));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        addStaticEffect(new SetOpponentMaximumHandSizeToSevenMinusCardTypesInGraveyardEffect());
    }
}
