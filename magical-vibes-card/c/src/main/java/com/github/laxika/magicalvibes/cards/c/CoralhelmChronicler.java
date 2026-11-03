package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasKickerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "54")
public class CoralhelmChronicler extends Card {

    public CoralhelmChronicler() {
        addTrigger(Triggers.youCastSpell().kicked(),
                new DrawCardEffect(),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        5, new CardHasKickerPredicate()));
    }
}
