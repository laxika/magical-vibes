package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "220")
public class TheKingpinOfCrime extends Card {

    public TheKingpinOfCrime() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new MayPayManaEffect("{W/B}",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true),
                        "Pay {W/B} to extort?")),
                (String) null));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new MayPayLifeEffect(
                2,
                new GrantPlayerStaticEffectsUntilEndOfTurnEffect(List.of(
                        new AssignCombatDamageWithToughnessEffect(
                                GrantScope.ALL_OWN_CREATURES,
                                new PermanentToughnessGreaterThanPowerPredicate()))),
                "Pay 2 life to have creatures you control assign combat damage using toughness?"));
    }
}
