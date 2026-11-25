package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "85")
@CardRegistration(set = "ME4", collectorNumber = "165")
public class ScarwoodBandits extends Card {

    public ScarwoodBandits() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(UnlessPaysEffect.unlessPays(Subjects.eachOpponent(), "{2}",
                                new GainControlOfTargetEffect(Duration.WHILE_SOURCE_REMAINS))
                        .withPrompt("Pay {2} to keep Scarwood Bandits from gaining control of the target artifact?")),
                "{2}{G}, {T}: Unless an opponent pays {2}, gain control of target artifact for as long as Scarwood Bandits remains on the battlefield.",
                TargetFilters.artifact()
        ));
    }
}
