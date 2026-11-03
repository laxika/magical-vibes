package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsOwnCommander;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "40")
@CardRegistration(set = "C18", collectorNumber = "4")
public class LoyalUnicorn extends Card {

    public LoyalUnicorn() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerControlsOwnCommander()),
                SequenceEffect.of(
                                PreventDamageEffect.allCombatToControlledMatchingPermanents(
                                        new PermanentIsCreaturePredicate()),
                                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES)
                        ));
    }
}
