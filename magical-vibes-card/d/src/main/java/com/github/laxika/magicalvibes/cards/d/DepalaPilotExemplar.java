package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayXManaRevealTopCardsToHandRestToBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "178")
@CardRegistration(set = "SLD", collectorNumber = "464")
@CardRegistration(set = "KLR", collectorNumber = "192")
public class DepalaPilotExemplar extends Card {

    public DepalaPilotExemplar() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DWARF)));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addTrigger(Triggers.becomesTapped(),
                new PayXManaRevealTopCardsToHandRestToBottomRandomEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.DWARF),
                                new CardSubtypePredicate(CardSubtype.VEHICLE)))));
    }
}
