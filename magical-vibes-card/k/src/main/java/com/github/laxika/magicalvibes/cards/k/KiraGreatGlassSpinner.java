package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "40")
@CardRegistration(set = "MMA", collectorNumber = "49")
@CardRegistration(set = "SLD", collectorNumber = "421")
public class KiraGreatGlassSpinner extends Card {

    public KiraGreatGlassSpinner() {
        // Flying is auto-loaded from Scryfall.

        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                        new CounterSpellEffect()),
                GrantScope.ALL_OWN_CREATURES));
    }
}
