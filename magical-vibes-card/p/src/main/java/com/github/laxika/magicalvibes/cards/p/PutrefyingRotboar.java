package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveSpellCastLifeLossToDefendingHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "10")
public class PutrefyingRotboar extends Card {

    public PutrefyingRotboar() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.BOAR),
                        new PerpetuallyGiveSpellCastLifeLossToDefendingHandEffect()));
    }
}
