package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
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

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "38")
public class NarsetEnlightenedExile extends Card {

    private static final CardAllOfPredicate NONCREATURE_NONLAND_BELOW_POWER = new CardAllOfPredicate(List.of(
            new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
            new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
            new CardManaValueLessThanSourcePowerPredicate()));

    public NarsetEnlightenedExile() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))),
                GrantScope.ALL_OWN_CREATURES));

        target(new GraveyardCardPredicateTargetFilter(
                NONCREATURE_NONLAND_BELOW_POWER, GraveyardSearchScope.ALL_GRAVEYARDS));
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        NONCREATURE_NONLAND_BELOW_POWER, GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
