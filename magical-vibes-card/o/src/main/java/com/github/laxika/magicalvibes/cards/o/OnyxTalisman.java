package com.github.laxika.magicalvibes.cards.o;

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

@CardRegistration(set = "ICE", collectorNumber = "331")
public class OnyxTalisman extends Card {

    public OnyxTalisman() {
        // Whenever a player casts a black spell, you may pay {3}. If you do, untap target permanent.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.BLACK),
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}",
                TargetFilters.permanent()
        ));
    }
}
