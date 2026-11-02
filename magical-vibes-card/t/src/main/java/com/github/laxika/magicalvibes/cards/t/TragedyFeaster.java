package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntGainLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "102")
@CardRegistration(set = "SOS", collectorNumber = "293")
public class TragedyFeaster extends Card {

    public TragedyFeaster() {
        // Trample is auto-loaded from Scryfall.

        // Ward—Discard a card. (Whenever this creature becomes the target of a spell or ability
        // an opponent controls, counter it unless that player discards a card.)
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessDiscardsEffect());

        // Infusion — At the beginning of your end step, sacrifice a permanent unless you gained
        // life this turn. The sacrifice only happens at resolution if you didn't gain life.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), ConditionalEffect.unless(
                new DidntGainLifeThisTurn(),
                new SacrificePermanentThenEffect(new PermanentTruePredicate(), null, "a permanent")));
    }
}
