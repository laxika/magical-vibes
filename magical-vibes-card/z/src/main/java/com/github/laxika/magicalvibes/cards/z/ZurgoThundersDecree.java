package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControllerEndStep;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "237")
public class ZurgoThundersDecree extends Card {

    public ZurgoThundersDecree() {
        addTrigger(Triggers.attacks(),
                new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true),
                new SacrificeCreatedPermanentsAtEndStepEffect());
        addStaticEffect(new ConditionalEffect(new ControllerEndStep(),
                new GrantEffectEffect(new CantBeSacrificedEffect(), GrantScope.OWN_PERMANENTS,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR))))));
    }
}
