package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "327")
public class LapisLazuliTalisman extends Card {

    public LapisLazuliTalisman() {
        // Whenever a player casts a blue spell, you may pay {3}. If you do, untap target permanent.
        // MayEffect models the "you may pay {3}" decision; accepting pays the cost and then a
        // target is chosen from the card's filter (see MayAbilityHandlerService).
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardColorPredicate(CardColor.BLUE),
                        List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                        "{3}"),
                "Pay {3} to untap target permanent?"
        ));
    }
}
