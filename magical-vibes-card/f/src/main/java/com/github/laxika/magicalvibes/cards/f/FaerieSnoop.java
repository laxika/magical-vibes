package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "203")
public class FaerieSnoop extends Card {

    public FaerieSnoop() {
        addMorph("{1}{U/B}{U/B}");
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new ConditionalEffect(new SourceIsFaceDown(), new CounterUnlessPaysEffect(2)));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1));
    }
}
