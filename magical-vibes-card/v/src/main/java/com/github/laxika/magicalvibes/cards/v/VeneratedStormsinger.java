package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "97")
public class VeneratedStormsinger extends Card {

    private static final SequenceEffect DEATH_TRIGGER = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
            new GainLifeEffect(1));

    public VeneratedStormsinger() {
        addTrigger(Triggers.attacks(),
                new CreateTokenEffect(1, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true));
        addTrigger(Triggers.attacks(), new SacrificeCreatedPermanentsAtEndStepEffect());
        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), DEATH_TRIGGER);
    }
}
