package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "320")
public class HematiteTalisman extends Card {

    public HematiteTalisman() {
        // Whenever a player casts a red spell, you may pay {3}. If you do, untap target permanent.
        target(TargetFilters.permanent()).addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardColorPredicate(CardColor.RED)),
                new MayPayManaEffect("{3}", new UntapPermanentsEffect(TapUntapScope.TARGET), "Pay {3}?"));
    }
}
