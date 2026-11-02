package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "210")
public class WanderingTroubadour extends Card {

    public WanderingTroubadour() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 1),
                new VentureIntoDungeonEffect()));
    }
}
