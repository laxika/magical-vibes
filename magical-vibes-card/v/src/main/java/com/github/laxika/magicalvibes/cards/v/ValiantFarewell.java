package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "31")
public class ValiantFarewell extends Card {

    public ValiantFarewell() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 0))
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                                new CardTypePredicate(CardType.CREATURE),
                                List.of(new PerpetuallyBoostTriggeringCardEffect(2, 0)))), true))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
