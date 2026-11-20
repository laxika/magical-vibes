package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;


@CardRegistration(set = "C19", collectorNumber = "35")
public class SelesnyaEulogist extends Card {

    public SelesnyaEulogist() {
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, creatureCard, false),
                        new PopulateEffect()),
                "{2}{G}: Exile target creature card from a graveyard, then populate.",
                new GraveyardCardPredicateTargetFilter(creatureCard, GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
