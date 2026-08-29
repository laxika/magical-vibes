package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "38")
@CardRegistration(set = "WWK", collectorNumber = "24")
public class AetherTradewinds extends Card {

    public AetherTradewinds() {
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                "Target must be a permanent you don't control"
        )).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
