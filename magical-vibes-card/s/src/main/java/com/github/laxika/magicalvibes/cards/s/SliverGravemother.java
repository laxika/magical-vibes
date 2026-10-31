package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantEncoreToCreatureCardsOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledSubtypeEffect;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "707")
@CardRegistration(set = "CMM", collectorNumber = "777")
@CardRegistration(set = "CMM", collectorNumber = "782")
public class SliverGravemother extends Card {

    public SliverGravemother() {
        addStaticEffect(new IgnoreLegendRuleForControlledSubtypeEffect(CardSubtype.SLIVER));
        addStaticEffect(new GrantEncoreToCreatureCardsOfSubtypeEffect(CardSubtype.SLIVER));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {5}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
