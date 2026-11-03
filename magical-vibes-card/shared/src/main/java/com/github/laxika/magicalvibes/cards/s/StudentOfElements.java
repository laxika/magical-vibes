package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TobitaMasterOfWinds;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "93")
public class StudentOfElements extends Card {

    public StudentOfElements() {
        setBackFaceCard(new TobitaMasterOfWinds());

        // "When this creature has flying, flip it." - state-triggered ability (CR 603.8);
        // the source predicate is layer-aware so granted flying (auras, pumps) counts.
        addTrigger(Triggers.sourceState(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                new TransformToBackFaceEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "TobitaMasterOfWinds";
    }
}
