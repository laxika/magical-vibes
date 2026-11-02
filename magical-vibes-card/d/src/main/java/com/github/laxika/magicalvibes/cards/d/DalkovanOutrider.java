package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTopCreatureCardInLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "20")
public class DalkovanOutrider extends Card {

    public DalkovanOutrider() {
        addTrigger(Triggers.attacks(),
                new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true));
        addTrigger(Triggers.attacks(), new SacrificeCreatedPermanentsAtEndStepEffect());
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                new PerpetuallyBoostTopCreatureCardInLibraryEffect(1));
    }
}
