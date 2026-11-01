package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of Garland, Knight of Cornelia. */
public class ChaosTheEndless extends Card {

    public ChaosTheEndless() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect());
    }
}
