package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "77")
public class JelennSphinx extends Card {

    public JelennSphinx() {
        // Whenever this creature attacks, other attacking creatures get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesEffect(1, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())))));
    }
}
