package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ChopDown;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "14")
public class GiantKiller extends Card {

    public GiantKiller() {
        setBackFaceCard(new ChopDown());
        addCastingOption(new AdventureCast("{2}{W}"));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChopDown";
    }
}
