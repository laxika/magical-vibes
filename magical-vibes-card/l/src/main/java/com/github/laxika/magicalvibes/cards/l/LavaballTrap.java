package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "135")
public class LavaballTrap extends Card {

    public LavaballTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{3}{R}{R}")),
                new OpponentPermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 2),
                false));

        target(landFilter("First target must be a land"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(landFilter("Second target must be a land"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4));
    }

    private static PermanentPredicateTargetFilter landFilter(String description) {
        return new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), description);
    }
}
