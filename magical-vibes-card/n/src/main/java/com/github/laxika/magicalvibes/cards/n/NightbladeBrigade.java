package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "85")
public class NightbladeBrigade extends Card {

    public NightbladeBrigade() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(1));
        addTrigger(Triggers.attacks(),
                new CreateTokenEffect(1, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true));
        addTrigger(Triggers.attacks(), new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
