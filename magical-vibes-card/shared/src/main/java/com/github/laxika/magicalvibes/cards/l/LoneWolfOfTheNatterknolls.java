package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class LoneWolfOfTheNatterknolls extends Card {

    public LoneWolfOfTheNatterknolls() {
        addTrigger(Triggers.opponentCastsSpell(),
                SpellCastTriggerEffect.duringYourTurn(null, List.of(new DrawCardEffect(2))));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
