package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "53")
public class SuddenStorm extends Card {

    public SuddenStorm() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new SkipNextUntapEffect(Subjects.target()))
                .addSpellEffect(new ScryEffect(1));
    }
}
