package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "251")
@CardRegistration(set = "DBL", collectorNumber = "251")
public class WinterthornBlessing extends Card {

    public WinterthornBlessing() {
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new SkipNextUntapEffect(Subjects.target()));

        addCastingOption(new FlashbackCast("{1}{G}{U}"));
    }
}
