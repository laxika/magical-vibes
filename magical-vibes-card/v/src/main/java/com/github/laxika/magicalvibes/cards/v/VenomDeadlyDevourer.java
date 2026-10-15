package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SPE", collectorNumber = "22")
public class VenomDeadlyDevourer extends Card {

    public VenomDeadlyDevourer() {
        var creatureCard = new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.ALL_GRAVEYARDS);
        var symbiote = new PermanentHasSubtypePredicate(CardSubtype.SYMBIOTE);
        var counterEffect = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new EventValue(), null, symbiote, false, null);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileTargetCreatureCardFromGraveyardThenReflexiveEffect(counterEffect)),
                "{3}: Exile target creature card from a graveyard. When you do, put X +1/+1 counters on target Symbiote, where X is the exiled card's toughness.",
                creatureCard));
    }
}
