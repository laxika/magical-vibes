package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "122")
@CardRegistration(set = "SPM", collectorNumber = "268")
@CardRegistration(set = "OM1", collectorNumber = "120")
public class WebOfLifeAndDestiny extends Card {

    public WebOfLifeAndDestiny() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        5, new CardTypePredicate(CardType.CREATURE)));
    }
}
