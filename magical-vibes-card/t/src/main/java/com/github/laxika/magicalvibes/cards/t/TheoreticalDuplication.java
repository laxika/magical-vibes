package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "34")
@CardRegistration(set = "WOC", collectorNumber = "112")
public class TheoreticalDuplication extends Card {

    public TheoreticalDuplication() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS),
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CreateTokenCopyOfEnteringPermanentEffect()))));
    }
}
