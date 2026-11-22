package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "56")
public class Waylay extends Card {

    public Waylay() {
        addSpellEffect(new CreateTokenEffect(3, "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(), Set.<CardType>of()));
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutCreated(Triggers.cleanup(),
                new ExileTargetPermanentEffect(Subjects.createdThisResolution())));
    }
}
