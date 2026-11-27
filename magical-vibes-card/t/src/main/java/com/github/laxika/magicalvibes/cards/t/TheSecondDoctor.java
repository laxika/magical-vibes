package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "156")
@CardRegistration(set = "WHO", collectorNumber = "440")
@CardRegistration(set = "WHO", collectorNumber = "553")
@CardRegistration(set = "WHO", collectorNumber = "761")
@CardRegistration(set = "WHO", collectorNumber = "1031")
@CardRegistration(set = "WHO", collectorNumber = "1144")
public class TheSecondDoctor extends Card {

    public TheSecondDoctor() {
        addStaticEffect(new PlayersHaveNoMaximumHandSizeEffect());
        // Each opponent who drew a card this way: a replaced draw is no draw.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                EachPlayerMayEffect.of(Subjects.eachPlayer(), "Draw a card?", players -> SequenceEffect.of(
                        new DrawCardEffect(1, players),
                        CreaturesCantAttackControllerUntilNextTurnEffect.duringTheirNextTurn(
                                Subjects.except(Subjects.playersThisWay(StepKind.DRAW), Subjects.you()),
                                Subjects.you()))));
    }
}
