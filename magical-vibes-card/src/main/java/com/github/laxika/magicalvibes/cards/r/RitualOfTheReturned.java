package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "JOU", collectorNumber = "80")
public class RitualOfTheReturned extends Card {

    public RitualOfTheReturned() {
        // Exile target creature card from your graveyard. Create a black Zombie creature token.
        // Its power is equal to that card's power and its toughness is equal to that card's toughness.
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addEffect(EffectSlot.SPELL, new ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect(
                CreateTokenEffect.blackZombie(1)));
    }
}
