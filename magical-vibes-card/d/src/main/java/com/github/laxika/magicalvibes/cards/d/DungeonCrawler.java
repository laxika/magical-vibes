package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "AFR", collectorNumber = "99")
public class DungeonCrawler extends Card {

    public DungeonCrawler() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_COMPLETES_DUNGEON,
                new MayEffect(new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Dungeon Crawler to your hand?"));
    }
}
