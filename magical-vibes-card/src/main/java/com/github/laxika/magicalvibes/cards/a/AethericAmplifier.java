package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRC", collectorNumber = "18")
@CardRegistration(set = "DRC", collectorNumber = "34")
public class AethericAmplifier extends Card {

    public AethericAmplifier() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Double the number of each kind of counter on target permanent.",
                                new DoubleCountersOnTargetPermanentEffect(), TargetFilters.permanent()),
                        new ChooseOneEffect.ChooseOneOption(
                                "Double the number of each kind of counter you have.",
                                new DoubleCountersOnControllerEffect())
                ))),
                "{4}, {T}: Choose one. Activate only as a sorcery.\n"
                        + "• Double the number of each kind of counter on target permanent.\n"
                        + "• Double the number of each kind of counter you have.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withModalChoiceAtActivation());
    }
}
