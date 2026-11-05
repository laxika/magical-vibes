package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;
import com.github.laxika.magicalvibes.model.Zone;

@CardRegistration(set = "WAR", collectorNumber = "53")
public class GodEternalKefnet extends Card {

    public GodEternalKefnet() {
        addTrigger(Triggers.youDrawCard(), new RevealFirstDrawInstantOrSorceryEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Kefnet into its owner's library third from the top?"));
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS), new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Kefnet into its owner's library third from the top?"));
    }
}
