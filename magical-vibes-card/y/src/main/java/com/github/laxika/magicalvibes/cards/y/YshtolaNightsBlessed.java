package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "7")
@CardRegistration(set = "FIC", collectorNumber = "191")
@CardRegistration(set = "FIC", collectorNumber = "207")
@CardRegistration(set = "FIC", collectorNumber = "215")
@CardRegistration(set = "FIC", collectorNumber = "226")
public class YshtolaNightsBlessed extends Card {

    public YshtolaNightsBlessed() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new AnyPlayerLostLifeThisTurn(4)),
                new DrawCardEffect(1));

        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardMinManaValuePredicate(3)
                        ))),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(2));
    }
}
