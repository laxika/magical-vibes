package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToTargetCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSOS", collectorNumber = "8")
public class ConsumedByHistory extends Card {

    public ConsumedByHistory() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.nontokenCreatureDies(ObjectRelation.ANY),
                new PerpetuallyGrantGraveyardAbilityToTargetCardEffect(Card.unearthAbility("{5}")))));
        addSpellEffect(new MassDamageEffect(3));
    }
}
