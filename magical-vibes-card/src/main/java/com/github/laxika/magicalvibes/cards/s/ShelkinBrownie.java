package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BandsWithOtherEffect;
import com.github.laxika.magicalvibes.model.effect.SuppressStaticEffectOnTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "204")
public class ShelkinBrownie extends Card {

    public ShelkinBrownie() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SuppressStaticEffectOnTargetUntilEndOfTurnEffect(BandsWithOtherEffect.class)),
                "{T}: Target creature loses all \"bands with other\" abilities until end of turn.",
                TargetFilters.creature()
        ));
    }
}
