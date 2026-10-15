package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "110")
public class StrongholdArena extends Card {

    public StrongholdArena() {
        // "Kicker {G} and/or {W}" — the two optional payments are independent and each may be
        // paid at most once.
        addEffect(EffectSlot.STATIC, new KickerEffect("{G}"));
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.singlePayment(List.of("{W}")));

        // Each paid kicker gains 3 life when Stronghold Arena enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), new GainLifeEffect(3)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new Scaled(new RepeatedAdditionalCostCount("{W}"), 3)));

        // The trigger is batched when one or more creatures deal combat damage in a damage step.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null,
                        new MayEffect(new RevealTopCardPutIntoHandAndChangeLifeEffect(false),
                                "Reveal the top card of your library?"), false, true));
    }
}
