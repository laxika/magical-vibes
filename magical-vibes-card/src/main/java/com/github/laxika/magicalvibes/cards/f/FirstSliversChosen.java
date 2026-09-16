package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "9")
public class FirstSliversChosen extends Card {

    public FirstSliversChosen() {
        // First Sliver's Chosen is itself a Sliver, so ALL_OWN_CREATURES includes it when it
        // matches the filter. Each Sliver gets the exalted attack trigger.
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
