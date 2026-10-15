package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.FullParty;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "443")
public class TheDestinedWarrior extends Card {

    public TheDestinedWarrior() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.CLERIC),
                        new CardSubtypePredicate(CardSubtype.ROGUE),
                        new CardSubtypePredicate(CardSubtype.WARRIOR),
                        new CardSubtypePredicate(CardSubtype.WIZARD)
                )), 1, CostModificationScope.SELF));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalReplacementEffect(
                new FullParty(),
                new BoostAllOwnCreaturesEffect(1, 0),
                new BoostAllOwnCreaturesEffect(3, 0)));
    }
}
