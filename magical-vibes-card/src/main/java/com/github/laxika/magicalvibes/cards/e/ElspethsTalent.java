package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringLoyaltyAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "72")
@CardRegistration(set = "MOC", collectorNumber = "80")
public class ElspethsTalent extends Card {

    public ElspethsTalent() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        +1,
                        List.of(CreateTokenEffect.whiteSoldier(3)),
                        "+1: Create three 1/1 white Soldier creature tokens."),
                GrantScope.ENCHANTED_PERMANENT,
                new PermanentIsPlaneswalkerPredicate()));

        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY,
                new TriggeringLoyaltyAbilityConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsPlaneswalkerPredicate(),
                                        new PermanentIsHostOfSourceAuraPredicate())),
                                SequenceEffect.of(
                                        new BoostAllOwnCreaturesEffect(2, 2),
                                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES)))));
    }
}
