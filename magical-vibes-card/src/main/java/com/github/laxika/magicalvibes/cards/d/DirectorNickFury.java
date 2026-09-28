package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "81")
@CardRegistration(set = "MSC", collectorNumber = "399")
public class DirectorNickFury extends Card {

    public DirectorNickFury() {
        CardSubtypePredicate hero = new CardSubtypePredicate(CardSubtype.HERO);
        addEffect(EffectSlot.STATIC,
                new ReduceCastCostForMatchingSpellsEffect(hero, 1, CostModificationScope.SELF));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, hero));
    }
}
