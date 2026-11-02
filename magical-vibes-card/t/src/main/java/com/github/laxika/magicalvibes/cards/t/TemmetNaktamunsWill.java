package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRC", collectorNumber = "4")
public class TemmetNaktamunsWill extends Card {

    public TemmetNaktamunsWill() {
        addTrigger(Triggers.youAttack(),
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)));

        addTrigger(Triggers.youDrawCard(),
                new BoostAllOwnCreaturesEffect(1, 1,
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
    }
}
