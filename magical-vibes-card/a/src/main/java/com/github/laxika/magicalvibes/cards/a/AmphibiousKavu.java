package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "78")
public class AmphibiousKavu extends Card {

    public AmphibiousKavu() {
        PermanentColorInPredicate blueOrBlack =
                new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK));
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK,
                new BoostSelfWhenCombatOpponentMatchesEffect(blueOrBlack, 3, 3));
        addTrigger(Triggers.becomesBlocked(),
                new BoostSelfWhenCombatOpponentMatchesEffect(blueOrBlack, 3, 3));
    }
}
