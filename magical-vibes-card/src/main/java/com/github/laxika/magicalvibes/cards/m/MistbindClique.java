package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "LRW", collectorNumber = "75")
public class MistbindClique extends Card {

    public MistbindClique() {
        // Champion a Faerie.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect(CardSubtype.FAERIE));
        // When a Faerie is championed with this creature, tap all lands target player controls.
        addEffect(EffectSlot.ON_CHAMPIONED,
                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, new PermanentIsLandPredicate()));
    }
}
