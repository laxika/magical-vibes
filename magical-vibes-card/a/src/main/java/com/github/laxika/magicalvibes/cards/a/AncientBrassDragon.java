package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "141")
public class AncientBrassDragon extends Card {

    public AncientBrassDragon() {
        ReturnTargetCardsFromGraveyardToBattlefieldEffect reanimateCreatures =
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.CREATURE), 0, false, false,
                        null, 0, new EventValue(), null, null, null, 0,
                        GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false, 0, 0);
        QueueReflexiveAbilityEffect reanimateAfterRoll =
                new QueueReflexiveAbilityEffect(reanimateCreatures);

        // Whenever this creature deals combat damage to a player, roll a d20. When you do, put any
        // number of target creature cards with total mana value X or less from graveyards onto the
        // battlefield under your control, where X is the result.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new RollD20Effect(reanimateAfterRoll, reanimateAfterRoll));
    }
}
