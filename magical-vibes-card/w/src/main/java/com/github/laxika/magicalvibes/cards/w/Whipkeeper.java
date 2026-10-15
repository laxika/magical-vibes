package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPermanentThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "228")
public class Whipkeeper extends Card {

    public Whipkeeper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(new DamageDealtToTargetPermanentThisTurn())),
                "{T}: This creature deals damage to target creature equal to the damage already dealt to it this turn.",
                TargetFilters.creature()
        ));
    }
}
