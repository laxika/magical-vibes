package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "INV", collectorNumber = "185")
public class ElfhameSanctuary extends Card {

    public ElfhameSanctuary() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                        new SkipDrawStepThisTurnEffect()),
                "Search your library for a basic land card?"));
    }
}
