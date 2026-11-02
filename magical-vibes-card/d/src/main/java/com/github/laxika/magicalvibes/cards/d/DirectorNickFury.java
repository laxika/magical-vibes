package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "81")
@CardRegistration(set = "MSC", collectorNumber = "399")
public class DirectorNickFury extends Card {

    public DirectorNickFury() {
        CardSubtypePredicate hero = new CardSubtypePredicate(CardSubtype.HERO);
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(hero, 1, CostModificationScope.SELF));
        addTrigger(Triggers.youAttack(),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, hero));
    }
}
