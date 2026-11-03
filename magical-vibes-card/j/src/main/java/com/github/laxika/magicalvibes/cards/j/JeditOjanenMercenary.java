package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "34")
@CardRegistration(set = "DMC", collectorNumber = "56")
public class JeditOjanenMercenary extends Card {

    public JeditOjanenMercenary() {
        CreateTokenEffect catWarriorToken = new CreateTokenEffect(
                "Cat Warrior", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.CAT, CardSubtype.WARRIOR),
                Set.of(Keyword.FORESTWALK), Set.of());
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                                        new PermanentIsSourceCardPredicate())))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new MayPayManaEffect("{G}", catWarriorToken,
                        "Pay {G} to create a 2/2 green Cat Warrior creature token with forestwalk?"));
    }
}
