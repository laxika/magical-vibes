package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "13")
@CardRegistration(set = "MOC", collectorNumber = "100")
public class DarksteelSplicer extends Card {

    public DarksteelSplicer() {
        CreateTokenEffect golemToken = new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)), "Phyrexian Golem", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT));

        addTrigger(Triggers.enters(ObjectRelation.THIS), golemToken);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.NONTOKEN_CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                golemToken);

        addStaticEffect(new StaticBoostEffect(0, 0, Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.ALL_OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.GOLEM)));
    }
}
