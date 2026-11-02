package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "327")
public class LapisLazuliTalisman extends Card {

    public LapisLazuliTalisman() {
        // Whenever a player casts a blue spell, you may pay {3}. If you do, untap target permanent.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.BLUE),
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}",
                TargetFilters.permanent()
        ));
    }
}
