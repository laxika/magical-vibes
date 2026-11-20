package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SOI", collectorNumber = "134")
public class ShambleBack extends Card {

    public ShambleBack() {
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS))
                .addSpellEffect(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                        1, new CardTypePredicate(CardType.CREATURE), false));
        addSpellEffect(CreateTokenEffect.blackZombie(1));
        addSpellEffect(new GainLifeEffect(2));
    }
}
