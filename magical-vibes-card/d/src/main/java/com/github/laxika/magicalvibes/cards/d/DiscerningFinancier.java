package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "10")
public class DiscerningFinancier extends Card {

    public DiscerningFinancier() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentControlsMoreLands()),
                CreateTokenEffect.ofTreasureToken(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new ChooseAnotherPlayerGainsControlOfTargetPermanentEffect(), new DrawCardEffect(1)),
                "{2}{W}: Choose another player. That player gains control of target Treasure you control. You draw a card.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.TREASURE),
                        "Target must be a Treasure you control")));
    }
}
