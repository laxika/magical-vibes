package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "14")
@CardRegistration(set = "DDO", collectorNumber = "9")
public class GempalmAvenger extends Card {

    public GempalmAvenger() {
        PermanentHasSubtypePredicate soldier = new PermanentHasSubtypePredicate(CardSubtype.SOLDIER);
        addTrigger(Triggers.youCycleThisCard(), SequenceEffect.of(
                BoostEffect.allCreatures(1, 1, soldier),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.allCreatures(), soldier)));
        addCycling("{2}{W}");
    }
}
