package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AttachChosenPermanentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class TheInvincibleIronMan extends Card {

    public TheInvincibleIronMan() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                new PutCardToBattlefieldThenEffect(
                        new CardTypePredicate(CardType.ARTIFACT), "artifact",
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        new AttachChosenPermanentToSourceEffect()),
                "Put an artifact card from your hand onto the battlefield?"));
    }
}
