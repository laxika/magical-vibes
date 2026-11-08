package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "64")
@CardRegistration(set = "MSC", collectorNumber = "376")
public class AncestralCommunion extends Card {

    public AncestralCommunion() {
        // "When you cast this spell while you control your commander": the event condition.
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withEventCondition(new ControlledCommanderAsCast()),
                new CopyTriggeringSpellEffect());
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build());
    }
}
