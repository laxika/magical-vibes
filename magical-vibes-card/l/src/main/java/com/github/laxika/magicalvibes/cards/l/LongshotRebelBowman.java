package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "120")
public class LongshotRebelBowman extends Card {

    public LongshotRebelBowman() {
        // Noncreature spells you cast cost {1} less to cast.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                1,
                CostModificationScope.SELF));

        // Whenever you cast a noncreature spell, Longshot deals 2 damage to each opponent.
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
    }
}
