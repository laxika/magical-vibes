package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "320")
public class HematiteTalisman extends Card {

    public HematiteTalisman() {
        // Whenever a player casts a red spell, you may pay {3}. If you do, untap target permanent.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}",
                TargetFilters.permanent()
        ));
    }
}
