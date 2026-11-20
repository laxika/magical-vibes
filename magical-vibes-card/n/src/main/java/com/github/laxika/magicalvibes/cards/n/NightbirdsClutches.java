package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "154")
@CardRegistration(set = "UMA", collectorNumber = "141")
public class NightbirdsClutches extends Card {

    public NightbirdsClutches() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new CantBlockThisTurnEffect(Subjects.target()));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
