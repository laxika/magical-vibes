package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "327")
public class LapisLazuliTalisman extends Card {

    public LapisLazuliTalisman() {
        // Whenever a player casts a blue spell, you may pay {3}. If you do, untap target permanent.
        target(TargetFilters.permanent()).addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardColorPredicate(CardColor.BLUE)),
                new MayPayManaEffect("{3}", new UntapPermanentsEffect(TapUntapScope.TARGET), "Pay {3}?"));
    }
}
