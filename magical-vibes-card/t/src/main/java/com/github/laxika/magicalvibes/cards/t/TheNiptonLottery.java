package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "PIP", collectorNumber = "113")
@CardRegistration(set = "PIP", collectorNumber = "423")
@CardRegistration(set = "PIP", collectorNumber = "641")
@CardRegistration(set = "PIP", collectorNumber = "951")
public class TheNiptonLottery extends Card {

    public TheNiptonLottery() {
        addSpellEffect(ChoosePermanentsEffect.atRandom(1, Subjects.allCreatures(), "a creature"));
        addSpellEffect(GainControlOfTargetEffect.of(Subjects.thisWay(StepKind.CHOOSE), Duration.UNTIL_END_OF_TURN));
        addSpellEffect(new UntapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
        addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.thisWay(StepKind.CHOOSE)));
        addSpellEffect(DestroyAllPermanentsEffect.sparing(
                new PermanentIsCreaturePredicate(), Subjects.thisWay(StepKind.CHOOSE)));
    }
}
