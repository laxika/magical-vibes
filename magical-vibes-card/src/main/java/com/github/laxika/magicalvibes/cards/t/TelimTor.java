package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "197")
public class TelimTor extends Card {

    public TelimTor() {
        // Flanking is engine-handled via the FLANKING keyword loaded from Scryfall.
        // Whenever Telim'Tor attacks, all attacking creatures with flanking get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(1, 1, new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLANKING)))));
    }
}
