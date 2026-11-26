package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "268")
@CardRegistration(set = "DMR", collectorNumber = "166")
@CardRegistration(set = "ARC", collectorNumber = "61")
public class KamahlFistOfKrosa extends Card {

    public KamahlFistOfKrosa() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new AnimatePermanentsEffect(
                        1, 1, List.of(), Set.of(), null, Set.of(),
                        Subjects.target(), Duration.UNTIL_END_OF_TURN)),
                "{G}: Target land becomes a 1/1 creature until end of turn. It's still a land.",
                TargetFilters.land()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}{G}",
                List.of(
                        BoostEffect.creaturesYouControl(3, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.creaturesYouControl())
                ),
                "{2}{G}{G}{G}: Creatures you control get +3/+3 and gain trample until end of turn."
        ));
    }
}
