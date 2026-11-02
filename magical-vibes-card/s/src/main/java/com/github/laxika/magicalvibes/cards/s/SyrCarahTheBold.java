package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "145")
public class SyrCarahTheBold extends Card {

    public SyrCarahTheBold() {
        addTrigger(Triggers.dealsDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.instantOrSorceryYouControlDealsDamage(),
                new ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Syr Carah deals 1 damage to any target."
        ));
    }
}
