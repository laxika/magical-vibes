package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "110")
public class ChaosDefiler extends Card {

    public ChaosDefiler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), battleCannon());
        addTrigger(Triggers.dies(ObjectRelation.THIS), battleCannon());
    }

    /**
     * "For each opponent, choose a nonland permanent that player controls. Destroy one of them chosen at random": the
     * random pick is among those without indestructible while any was chosen (ruling, 2022-10-07).
     */
    private static CardEffect battleCannon() {
        return SequenceEffect.of(
                ChoosePermanentsEffect.oneForEach(Subjects.eachOpponent(),
                        Subjects.controlledBy(Subjects.eachOpponent(),
                                new PermanentNotPredicate(new PermanentIsLandPredicate())),
                        "a nonland permanent"),
                ChoosePermanentsEffect.atRandom(1, Subjects.thisWay(StepKind.CHOOSE,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.INDESTRUCTIBLE))),
                        "one of them"),
                new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
