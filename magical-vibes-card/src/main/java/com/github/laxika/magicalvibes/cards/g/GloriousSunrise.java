package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "200")
public class GloriousSunrise extends Card {

    public GloriousSunrise() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 and gain trample until end of turn.",
                        List.of(
                                new BoostAllOwnCreaturesEffect(1, 1),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target land gains \"{T}: Add {G}{G}{G}\" until end of turn.",
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new AwardManaEffect(ManaColor.GREEN, 3)),
                                        "{T}: Add {G}{G}{G}."),
                                GrantScope.TARGET,
                                null,
                                EffectDuration.UNTIL_END_OF_TURN),
                        TargetFilters.land()),
                new ChooseOneEffect.ChooseOneOption(
                        "Draw a card if you control a creature with power 3 or greater.",
                        ConditionalEffect.unless(
                                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(3)))),
                                new DrawCardEffect(1))),
                new ChooseOneEffect.ChooseOneOption("You gain 3 life.", new GainLifeEffect(3))
        )));
    }
}
