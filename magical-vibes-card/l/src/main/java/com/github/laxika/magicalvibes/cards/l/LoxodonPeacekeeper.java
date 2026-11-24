package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PlayerHasLeastLifePredicate;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "13")
public class LoxodonPeacekeeper extends Card {

    public LoxodonPeacekeeper() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ChoosePlayerEffect(Subjects.matching(Subjects.eachPlayer(), new PlayerHasLeastLifePredicate()),
                        "one of the players tied for lowest life total"),
                GainControlOfTargetEffect.of(Subjects.source(), Duration.PERMANENT)
                        .gainedBy(Subjects.playersThisWay(StepKind.CHOOSE)));
    }
}
