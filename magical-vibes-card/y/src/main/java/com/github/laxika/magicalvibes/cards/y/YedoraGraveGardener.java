package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MUL", collectorNumber = "30")
@CardRegistration(set = "MUL", collectorNumber = "95")
@CardRegistration(set = "MUL", collectorNumber = "160")
@CardRegistration(set = "CMM", collectorNumber = "333")
@CardRegistration(set = "C21", collectorNumber = "70")
@CardRegistration(set = "DSC", collectorNumber = "209")
@CardRegistration(set = "MKC", collectorNumber = "197")
public class YedoraGraveGardener extends Card {

    public YedoraGraveGardener() {
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .faceDown(0, 0, Set.of(CardType.LAND), Set.of(CardSubtype.FOREST)),
                        "Return it to the battlefield face down as a Forest?"));
    }
}
