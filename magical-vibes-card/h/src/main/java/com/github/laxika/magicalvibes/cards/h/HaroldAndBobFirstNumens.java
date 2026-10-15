package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DyingPermanentWasCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAsAuraEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "78")
@CardRegistration(set = "PIP", collectorNumber = "398")
@CardRegistration(set = "PIP", collectorNumber = "606")
@CardRegistration(set = "PIP", collectorNumber = "926")
public class HaroldAndBobFirstNumens extends Card {

    public HaroldAndBobFirstNumens() {
        ControlledPermanentPredicateTargetFilter forestYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                "Target must be a Forest you control");

        addEffect(EffectSlot.ON_DEATH, new DyingPermanentWasCreatureConditionalEffect(
                ReturnSourceAsAuraEffect.losingOtherAbilities(forestYouControl)));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null,
                        List.of(new AwardAnyColorManaEffect(3), new GiveControllerRadCountersEffect(2)),
                        "{T}: Add three mana of any one color. You get two rad counters."),
                GrantScope.ENCHANTED_PERMANENT));
    }
}
