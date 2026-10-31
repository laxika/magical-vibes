package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "34")
public class StudyBreak extends Card {

    public StudyBreak() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET));

        addSpellEffect(new LearnEffect());
    }
}
