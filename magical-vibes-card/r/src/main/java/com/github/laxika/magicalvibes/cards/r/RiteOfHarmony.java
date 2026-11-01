package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "236")
@CardRegistration(set = "DBL", collectorNumber = "236")
public class RiteOfHarmony extends Card {

    public RiteOfHarmony() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.enters(Triggers.CREATURE_OR_ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                new DrawCardEffect())));
        addCastingOption(new FlashbackCast("{2}{G}{W}"));
    }
}
