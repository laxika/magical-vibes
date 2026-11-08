package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "289")
@CardRegistration(set = "C19", collectorNumber = "39")
public class ChainerNightmareAdept extends Card {

    public ChainerNightmareAdept() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                                new CardTypePredicate(CardType.CREATURE), true)
                ),
                "Discard a card: You may cast a creature spell from your graveyard this turn. "
                        + "Activate only once each turn.",
                1));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.NONTOKEN,
                                        new PermanentNotPredicate(new PermanentCastFromZonePredicate(Zone.HAND)))))),
                        ObjectRelation.YOU_CONTROL),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT, GrantDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
