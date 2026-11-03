package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "329")
public class NacreTalisman extends Card {

    public NacreTalisman() {
        // Whenever a player casts a white spell, you may pay {3}. If you do, untap target permanent.
        target(TargetFilters.permanent()).addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardColorPredicate(CardColor.WHITE)),
                new MayPayManaEffect("{3}", new UntapPermanentsEffect(TapUntapScope.TARGET), "Pay {3}?"));
    }
}
