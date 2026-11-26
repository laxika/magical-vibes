package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/** Back face of {@link com.github.laxika.magicalvibes.cards.v.VincentValentine}. */
public class GalianBeast extends Card {

    public GalianBeast() {
        // "Return it to the battlefield tapped" names no controller: it returns under this ability's controller's (CR 110.2a).
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard())
                .enteringTapped().underControlOf(Subjects.you()));
    }
}
