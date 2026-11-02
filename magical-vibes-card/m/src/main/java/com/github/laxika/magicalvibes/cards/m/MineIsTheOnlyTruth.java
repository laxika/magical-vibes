package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "340")
public class MineIsTheOnlyTruth extends Card {

    public MineIsTheOnlyTruth() {
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect())));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConditionalEffect(new ControllerDrewAtLeastCardsLastTurn(1), new SacrificeSelfEffect()));
    }
}
