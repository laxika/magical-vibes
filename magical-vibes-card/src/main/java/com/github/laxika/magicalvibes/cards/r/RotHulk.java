package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "851")
public class RotHulk extends Card {

    public RotHulk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE),
                new Sum(new PlayersInGame(), new Fixed(-1))));
    }
}
