package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YOTJ", collectorNumber = "17")
public class JessieZaneFangbringer extends Card {

    public JessieZaneFangbringer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), conjureAmbushViper());
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.SNAKE)),
                conjureAmbushViper());
    }

    /** Ambush Viper, which perpetually gains "When this creature enters, draw a card." */
    private static ConjureCardsEffect conjureAmbushViper() {
        return ConjureCardsEffect.named("ISD", "169", ConjureDestination.intoTopCardsOfLibraryAtRandom(6))
                .withAbilities(List.of(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect())));
    }
}
