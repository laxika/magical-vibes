package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class ThroneOfDeath extends Card {

    public ThroneOfDeath() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MillEffect(1, MillRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE), new DrawCardEffect(1)),
                "{2}{B}, {T}, Exile a creature card from your graveyard: Draw a card."
        ));
    }
}
