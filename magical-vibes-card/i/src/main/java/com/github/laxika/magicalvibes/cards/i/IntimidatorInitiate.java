package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "96")
public class IntimidatorInitiate extends Card {

    public IntimidatorInitiate() {
        // Whenever a player casts a red spell, you may pay {1}. If you do, target creature can't block this turn.
        target(TargetFilters.creature())
                .addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                        new MayPayManaEffect("{1}", new CantBlockThisTurnEffect(TapUntapScope.TARGET), "Pay {1} to make target creature unable to block this turn?"));
    }
}
