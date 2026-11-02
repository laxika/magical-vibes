package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "160")
@CardRegistration(set = "FIN", collectorNumber = "366")
public class SummonBrynhildr extends Card {

    public SummonBrynhildr() {
        addTrigger(Triggers.chapter(1), ExileTopCardMayPlayThisTurnEffect.renewedOnLoreCounterTurns());

        var nextCreatureGainsHaste = new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE))),
                true,
                false);
        addTrigger(Triggers.chapter(2), nextCreatureGainsHaste);
        addTrigger(Triggers.chapter(3), nextCreatureGainsHaste);
    }
}
