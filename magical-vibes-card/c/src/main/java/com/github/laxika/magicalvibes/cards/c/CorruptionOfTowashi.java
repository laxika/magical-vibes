package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "53")
public class CorruptionOfTowashi extends Card {

    public CorruptionOfTowashi() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, incubatorToken());
        OncePerTurnTriggerEffect drawTrigger = OncePerTurnTriggerEffect.markOnAcceptance(
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
        addTrigger(Triggers.transforms(null, ObjectRelation.YOU_CONTROL), drawTrigger);
        addTrigger(Triggers.enters(Triggers.TRANSFORMED, ObjectRelation.YOU_CONTROL), drawTrigger);
    }

    private static CreateTokenEffect incubatorToken() {
        ActivatedAbility transform = new ActivatedAbility(
                false,
                "{2}",
                List.of(new TransformSelfEffect()),
                "{2}: Transform this token."
        );
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Incubator", 0, 0, null, null,
                List.of(), Set.of(), Set.of(), false, false, List.of(), List.of(transform),
                false, false, false, 4, Set.of()
        );
    }
}
