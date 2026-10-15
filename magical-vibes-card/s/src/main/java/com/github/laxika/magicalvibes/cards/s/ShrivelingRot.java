package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "54")
public class ShrivelingRot extends Card {

    public ShrivelingRot() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}{B}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, whenever a creature is dealt damage, destroy it",
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE,
                                new DestroyTargetPermanentEffect())),
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, whenever a creature dies, that creature's controller loses life equal to its toughness",
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                EffectSlot.ON_ANY_CREATURE_DIES,
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER))))));
    }
}
