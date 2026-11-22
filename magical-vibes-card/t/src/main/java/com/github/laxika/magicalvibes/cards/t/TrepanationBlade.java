package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "235")
@CardRegistration(set = "IMA", collectorNumber = "231")
public class TrepanationBlade extends Card {

    public TrepanationBlade() {
        // Whenever equipped creature attacks, defending player reveals cards from the top of
        // their library until they reveal a land card. The creature gets +1/+0 until end of turn
        // for each card revealed this way. That player puts the revealed cards into their graveyard.
        addTrigger(Triggers.attacks(),
                new RevealCardsFromTopUntilEffect(Subjects.defendingPlayer(), new CardTypePredicate(CardType.LAND)),
                new BoostSelfEffect(Subjects.triggering(), new AmountThisWay(StepKind.REVEAL), new Fixed(0)),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
