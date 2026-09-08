package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MAT", collectorNumber = "43")
public class RebuildTheCity extends Card {

    public RebuildTheCity() {
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL,
                new CreateTokenCopyOfTargetPermanentEffect(
                        3, List.of(), Set.of(CardType.CREATURE), 3, 3, Map.of(), null,
                        Set.of(Keyword.VIGILANCE, Keyword.MENACE)));
    }
}
