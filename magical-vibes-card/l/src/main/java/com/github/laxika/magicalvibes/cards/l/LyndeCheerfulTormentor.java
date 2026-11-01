package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachCurseToOpponentAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCurseAttachedToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "38")
@CardRegistration(set = "MIC", collectorNumber = "76")
public class LyndeCheerfulTormentor extends Card {

    public LyndeCheerfulTormentor() {
        addTrigger(Triggers.dies(Triggers.NONTOKEN, ObjectRelation.YOU_OWN),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.CURSE),
                        new RegisterDelayedReturnCurseAttachedToPlayerEffect(null, null)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new AttachCurseToOpponentAndDrawEffect(),
                        "Attach a Curse attached to you to an opponent and draw two cards?"));
    }
}
