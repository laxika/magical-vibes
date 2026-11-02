package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterWhenOpponentCastsSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "56")
@CardRegistration(set = "DDS", collectorNumber = "3")
@CardRegistration(set = "C14", collectorNumber = "105")
public class DeepSeaKraken extends Card {

    public DeepSeaKraken() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.opponentCastsSpell(), new RemoveTimeCounterWhenOpponentCastsSpellEffect());
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(),
                "Suspend 9—{2}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(9));
    }
}
