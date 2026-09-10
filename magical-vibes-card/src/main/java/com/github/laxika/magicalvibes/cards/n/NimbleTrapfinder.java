package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "72")
public class NimbleTrapfinder extends Card {

    public NimbleTrapfinder() {
        CardAnyOfPredicate partySubtype = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.CLERIC),
                new CardSubtypePredicate(CardSubtype.ROGUE),
                new CardSubtypePredicate(CardSubtype.WARRIOR),
                new CardSubtypePredicate(CardSubtype.WIZARD)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnotherPermanentEnteredThisTurn(partySubtype),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new FullParty(),
                new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new DrawCardEffect(1)));
    }
}
}
