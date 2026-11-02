package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "54")
public class ShrivelingRot extends Card {

    public ShrivelingRot() {
        addCastingCost(new EscalateManaCost("{2}{B}"));
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, whenever a creature is dealt damage, destroy it",
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.creatureDealtDamage(),
                                new DestroyTargetPermanentEffect()))),
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, whenever a creature dies, that creature's controller loses life equal to its toughness",
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY),
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER)))))));
    }
}
