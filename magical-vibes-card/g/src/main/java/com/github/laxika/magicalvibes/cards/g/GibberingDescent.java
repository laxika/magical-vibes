package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "66")
public class GibberingDescent extends Card {

    public GibberingDescent() {
        addCastingOption(new MadnessCast("{2}{B}{B}"));
        addStaticEffect(new ConditionalEffect(new ControllerHandEmpty(),
                new SkipDrawStepEffect(SkipStepOrPhaseKind.UPKEEP_STEP)));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), SequenceEffect.of(
                new LoseLifeEffect(1, Subjects.activePlayer()),
                new DiscardEffect(1, Subjects.triggeringPlayer())));
    }
}
