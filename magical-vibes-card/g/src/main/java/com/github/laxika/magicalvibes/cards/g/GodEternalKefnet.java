package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;

@CardRegistration(set = "WAR", collectorNumber = "53")
public class GodEternalKefnet extends Card {

    public GodEternalKefnet() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new RevealFirstDrawInstantOrSorceryEffect());
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Kefnet into its owner's library third from the top?"));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Kefnet into its owner's library third from the top?")));
    }
}
