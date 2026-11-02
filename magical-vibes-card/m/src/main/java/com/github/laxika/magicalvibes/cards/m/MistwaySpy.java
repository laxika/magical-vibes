package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "65")
public class MistwaySpy extends Card {

    public MistwaySpy() {
        addMorph("{1}{U}");
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new ConditionalEffect(new SourceIsFaceDown(), new CounterUnlessPaysEffect(2)));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                        CreateTokenEffect.ofClueToken(1))));
    }
}
