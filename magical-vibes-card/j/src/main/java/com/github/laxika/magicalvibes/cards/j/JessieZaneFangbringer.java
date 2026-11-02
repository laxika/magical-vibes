package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "17")
public class JessieZaneFangbringer extends Card {

    public JessieZaneFangbringer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureCardIntoTopCardsOfLibraryEffect(JessieZaneFangbringer::conjuredAmbushViper, 6));
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.SNAKE),
                        List.of(new ConjureCardIntoTopCardsOfLibraryEffect(
                                JessieZaneFangbringer::conjuredAmbushViper, 6))));
    }

    private static Card conjuredAmbushViper() {
        Card card = new AmbushViper();
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        return card;
    }
}
