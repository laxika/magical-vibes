package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "9")
public class HarvesterOfMisery extends Card {

    public HarvesterOfMisery() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllCreaturesEffect(-2, -2,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new BoostTargetCreatureEffect(-2, -2)),
                "{1}{B}, Discard this card: Target creature gets -2/-2 until end of turn.",
                TargetFilters.creature()));
    }
}
