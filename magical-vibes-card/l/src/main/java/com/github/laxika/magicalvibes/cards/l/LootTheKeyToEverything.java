package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BIG", collectorNumber = "21")
public class LootTheKeyToEverything extends Card {

    public LootTheKeyToEverything() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(1));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ExileTopCardMayPlayThisTurnEffect(
                new CardTypesAmongControlledPermanents(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        CountScope.CONTROLLER,
                        true),
                false));
    }
}
