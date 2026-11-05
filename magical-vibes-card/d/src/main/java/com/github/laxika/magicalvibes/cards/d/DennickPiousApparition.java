package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;

/** Back face of Dennick, Pious Apprentice. */
public class DennickPiousApparition extends Card {

    public DennickPiousApparition() {
        // Whenever one or more creature cards are put into graveyards from anywhere, investigate.
        // This ability triggers only once each turn.
        // One ability: its "triggers only once each turn" covers both conditions.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                        ObjectRelation.ANOTHER_YOU_OWN)),
                new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE,
                        ObjectRelation.OPPONENT_OWNS))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(CreateTokenEffect.ofClueToken(1)))));

        // If Dennick would be put into a graveyard from anywhere, exile it instead.
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
