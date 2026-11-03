package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "50")
public class GeralfTheFleshwright extends Card {

    public GeralfTheFleshwright() {
        addTrigger(Triggers.youCastSpell().duringYourTurn().nthOrLaterThisTurn(2),
                new CreateTokenEffect("Zombie Rogue", 2, 2, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.BLACK),
                        List.of(CardSubtype.ZOMBIE, CardSubtype.ROGUE)));

        var otherZombiesEnteredThisTurn = new Sum(
                new PermanentsEnteredBattlefieldThisTurn(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER),
                new Fixed(-1));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))),
                        ObjectRelation.YOU_CONTROL),
                new PutCountersOnEnteringCreatureEffect(otherZombiesEnteredThisTurn, false));
    }
}
