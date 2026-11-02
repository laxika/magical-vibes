package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.FullParty;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "72")
public class NimbleTrapfinder extends Card {

    public NimbleTrapfinder() {
        CardAnyOfPredicate partySubtype = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.CLERIC),
                new CardSubtypePredicate(CardSubtype.ROGUE),
                new CardSubtypePredicate(CardSubtype.WARRIOR),
                new CardSubtypePredicate(CardSubtype.WIZARD)));
        addStaticEffect(new ConditionalEffect(
                new AnotherPermanentEnteredThisTurn(partySubtype),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ConditionalEffect(
                new FullParty(),
                new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new DrawCardEffect(1)))));
    }
}
