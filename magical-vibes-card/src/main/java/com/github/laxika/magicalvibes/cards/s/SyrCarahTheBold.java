package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "145")
public class SyrCarahTheBold extends Card {

    public SyrCarahTheBold() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE,
                new ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Syr Carah deals 1 damage to any target."
        ));
    }
}
