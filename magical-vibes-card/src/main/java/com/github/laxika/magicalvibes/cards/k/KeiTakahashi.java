package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "78")
@CardRegistration(set = "LEG", collectorNumber = "238")
public class KeiTakahashi extends Card {

    public KeiTakahashi() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTargetCreature(2)),
                "{T}: Prevent the next 2 damage that would be dealt to target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
