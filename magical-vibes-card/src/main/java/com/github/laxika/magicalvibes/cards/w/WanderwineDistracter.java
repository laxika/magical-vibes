package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "82")
public class WanderwineDistracter extends Card {

    public WanderwineDistracter() {
        // Whenever this creature becomes tapped, target creature an opponent controls gets -3/-0 until end of turn.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new BoostTargetCreatureEffect(-3, 0, TargetFilters.creatureAnOpponentControls().predicate())));
    }
}
