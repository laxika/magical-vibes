package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PreviousActivePlayerCastTwoOrMoreSpellsLastTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class SeafaringWerewolf extends Card {

    public SeafaringWerewolf() {
        addStaticEffect(new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new PreviousActivePlayerCastTwoOrMoreSpellsLastTurn(), new TransformSelfEffect()));
    }
}
