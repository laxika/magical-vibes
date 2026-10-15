package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaSpentLessThanManaValuePredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "16")
@CardRegistration(set = "DSC", collectorNumber = "47")
public class AncientCellarspawn extends Card {

    public AncientCellarspawn() {
        CardAnyOfPredicate demonHorrorOrNightmare = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.DEMON),
                new CardSubtypePredicate(CardSubtype.HORROR),
                new CardSubtypePredicate(CardSubtype.NIGHTMARE)
        ));

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                demonHorrorOrNightmare, 1, CostModificationScope.SELF));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect()),
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                new StackEntryManaSpentLessThanManaValuePredicate()));
    }
}
