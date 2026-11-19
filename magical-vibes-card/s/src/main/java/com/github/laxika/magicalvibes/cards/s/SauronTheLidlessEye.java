package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "37")
@CardRegistration(set = "HOC", collectorNumber = "77")
public class SauronTheLidlessEye extends Card {

    public SauronTheLidlessEye() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                        new UntapPermanentsEffect(Subjects.target()),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{R}",
                List.of(
                        new BoostAllOwnCreaturesEffect(2, 0),
                        new LoseLifeEffect(2, Subjects.eachOpponent())),
                "{1}{B}{R}: Creatures you control get +2/+0 until end of turn. Each opponent loses 2 life."
        ));
    }
}
