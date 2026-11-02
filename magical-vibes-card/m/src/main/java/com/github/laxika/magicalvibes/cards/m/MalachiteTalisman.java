package com.github.laxika.magicalvibes.cards.m;

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

@CardRegistration(set = "ICE", collectorNumber = "328")
public class MalachiteTalisman extends Card {

    public MalachiteTalisman() {
        // Whenever a player casts a green spell, you may pay {3}. If you do, untap target permanent.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.GREEN),
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}",
                TargetFilters.permanent()
        ));
    }
}
