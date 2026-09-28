package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryWithFetchCountersAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;

@CardRegistration(set = "C20", collectorNumber = "13")
public class PakoArcaneRetriever extends Card {

    private static final String PARTNER_NAME = "Haldan, Avid Arcanist";

    public PakoArcaneRetriever() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Haldan, Avid Arcanist into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTopCardOfEachPlayersLibraryWithFetchCountersAndPutCountersOnSourceEffect());
    }
}
