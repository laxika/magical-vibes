package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "77")
@CardRegistration(set = "MOC", collectorNumber = "85")
public class RowansTalent extends Card {

    public RowansTalent() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        null,
                        List.of(
                                new BoostTargetCreatureEffect(2, 0),
                                new GrantKeywordEffect(
                                        Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE),
                                        GrantScope.TARGET,
                                        GrantDuration.END_OF_TURN)),
                        "+1: Up to one target creature gets +2/+0 and gains first strike and trample until end of turn.",
                        null,
                        +1,
                        null,
                        null,
                        List.of(),
                        0,
                        1),
                GrantScope.ENCHANTED_PERMANENT,
                new PermanentIsPlaneswalkerPredicate()));

        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(
                        null,
                        null,
                        false,
                        true,
                        null,
                        false,
                        false,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentIsHostOfSourceAuraPredicate()))));
    }
}
