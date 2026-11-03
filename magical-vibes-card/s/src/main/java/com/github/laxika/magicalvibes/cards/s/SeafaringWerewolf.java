package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PreviousActivePlayerCastTwoOrMoreSpellsLastTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class SeafaringWerewolf extends Card {

    public SeafaringWerewolf() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new PreviousActivePlayerCastTwoOrMoreSpellsLastTurn()),
                new TransformSelfEffect());
    }
}
