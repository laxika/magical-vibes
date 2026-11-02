package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "54")
public class EliteArcanist extends Card {

    public EliteArcanist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExileFromHandToImprintEffect(new CardTypePredicate(CardType.INSTANT), "an instant card"),
                        "You may exile an instant card from your hand."));
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new CopyImprintedCardAndMayCastCopyEffect()),
                "{X}, {T}: Copy the exiled card. You may cast the copy without paying its mana cost. X is the mana value of the exiled card."));
    }
}
