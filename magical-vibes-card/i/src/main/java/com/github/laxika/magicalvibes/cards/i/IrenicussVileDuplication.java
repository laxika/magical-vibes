package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "123")
public class IrenicussVileDuplication extends Card {

    public IrenicussVileDuplication() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.SPELL,
                new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(),
                        false, false, false, false, false, false, null, Set.of(Keyword.FLYING),
                        false, Map.of(), List.of(), false, true, new Fixed(1), false, Set.of(), false));
    }
}
