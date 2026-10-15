package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "57")
public class LongRiverLurker extends Card {

    public LongRiverLurker() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(1));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(1),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.FROG)));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MakeCreatureUnblockableEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new RegisterDelayedWatchedCreaturesCombatDamageEffect(
                                List.of(new MayEffect(
                                        new FlickerEffect(
                                                FlickerScope.SELF,
                                                null,
                                                ReturnTiming.IMMEDIATE,
                                                TurnStep.END_STEP,
                                                false,
                                                null,
                                                null,
                                                0,
                                                false,
                                                false),
                                        "Exile that creature, then return it to the battlefield under its owner's control?")),
                                false,
                                true));
    }
}
