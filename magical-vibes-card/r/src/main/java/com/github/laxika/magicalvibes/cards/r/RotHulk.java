package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "851")
@CardRegistration(set = "GNT", collectorNumber = "3")
@CardRegistration(set = "MB2", collectorNumber = "128")
@CardRegistration(set = "DRC", collectorNumber = "98")
public class RotHulk extends Card {

    public RotHulk() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new Sum(new PlayersInGame(), new Fixed(-1))));
    }
}
