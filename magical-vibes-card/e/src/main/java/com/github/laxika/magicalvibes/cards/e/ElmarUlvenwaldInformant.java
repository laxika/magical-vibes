package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "345")
@CardRegistration(set = "SLX", collectorNumber = "4")
public class ElmarUlvenwaldInformant extends Card {

    public ElmarUlvenwaldInformant() {
        // Whenever you cast your second spell each turn, untap target creature, then investigate.
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new UntapPermanentsEffect(TapUntapScope.TARGET),
                CreateTokenEffect.ofClueToken(1));
    }
}
