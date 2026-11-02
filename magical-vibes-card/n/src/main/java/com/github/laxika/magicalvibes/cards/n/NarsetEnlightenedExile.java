package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "38")
@CardRegistration(set = "MAT", collectorNumber = "88")
@CardRegistration(set = "MAT", collectorNumber = "138")
@CardRegistration(set = "MAT", collectorNumber = "173")
@CardRegistration(set = "MAT", collectorNumber = "217")
public class NarsetEnlightenedExile extends Card {

    private static final CardAllOfPredicate NONCREATURE_NONLAND_BELOW_POWER = new CardAllOfPredicate(List.of(
            new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
            new CardManaValueLessThanSourcePowerPredicate()));

    public NarsetEnlightenedExile() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1)))),
                GrantScope.ALL_OWN_CREATURES));

        target(new GraveyardCardPredicateTargetFilter(
                NONCREATURE_NONLAND_BELOW_POWER, GraveyardSearchScope.ALL_GRAVEYARDS));
        addTrigger(Triggers.attacks(),
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        NONCREATURE_NONLAND_BELOW_POWER, GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
