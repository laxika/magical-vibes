package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachCreatureTokenOnBattlefieldEffect;

@CardRegistration(set = "TOR", collectorNumber = "136")
public class ParallelEvolution extends Card {

    public ParallelEvolution() {
        addSpellEffect(new CreateTokenCopyOfEachCreatureTokenOnBattlefieldEffect());
        addCastingOption(new FlashbackCast("{4}{G}{G}{G}"));
    }
}
