package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOC", collectorNumber = "16")
@CardRegistration(set = "VOC", collectorNumber = "54")
public class TiminYouthfulGeist extends Card {

    private static final String PARTNER_NAME = "Rhoda, Geist Avenger";

    public TiminYouthfulGeist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
