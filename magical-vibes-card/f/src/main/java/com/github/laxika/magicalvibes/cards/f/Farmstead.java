package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SUM", collectorNumber = "19")
@CardRegistration(set = "3ED", collectorNumber = "19")
@CardRegistration(set = "2ED", collectorNumber = "20")
@CardRegistration(set = "LEA", collectorNumber = "19")
@CardRegistration(set = "LEB", collectorNumber = "20")
public class Farmstead extends Card {

    public Farmstead() {
        target(TargetFilters.land())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                        new MayPayManaEffect("{W}{W}", new GainLifeEffect(1),
                                "Pay {W}{W} to gain 1 life?")),
                        GrantScope.ENCHANTED_PERMANENT));
    }
}
