package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
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
        addStaticEffect(new KickerEffect("{G}"));
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{W}"), true, 1));

        // Each paid kicker gains 3 life when Stronghold Arena enters, as one life gain event.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new Sum(List.of(
                new FixedIfCondition(new Kicked(), 3, 0),
                new Scaled(new RepeatedAdditionalCostCount("{W}"), 3)))));

        // The trigger is batched when one or more creatures deal combat damage in a damage step.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null,
                        new MayEffect(new RevealTopCardPutIntoHandAndChangeLifeEffect(false),
                                "Reveal the top card of your library?"), false, true));
    }
}
