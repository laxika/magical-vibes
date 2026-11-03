package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentSubtypeAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "195")
@CardRegistration(set = "SIR", collectorNumber = "187")
public class BriarbridgePatrol extends Card {

    public BriarbridgePatrol() {
        addTrigger(Triggers.dealsDamage().withFilter(new PermanentIsCreaturePredicate()),
                CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControllerSacrificedPermanentSubtypeAtLeastThisTurn(3, CardSubtype.CLUE)),
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"));
    }
}
