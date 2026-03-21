package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "69")
public class TetsukoUmezawaFugitive extends Card {

    public TetsukoUmezawaFugitive() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new CantBeBlockedEffect(),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentPowerAtMostPredicate(1),
                        new PermanentToughnessAtMostPredicate(1)
                ))
        ));
    }
}
