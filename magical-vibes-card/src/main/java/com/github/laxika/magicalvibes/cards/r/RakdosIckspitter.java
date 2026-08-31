package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "128")
public class RakdosIckspitter extends Card {

    public RakdosIckspitter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DealDamageToTargetCreatureEffect(1),
                        new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PERMANENT_CONTROLLER)
                ),
                "{T}: Rakdos Ickspitter deals 1 damage to target creature and that creature's controller loses 1 life.",
                TargetFilters.creature()
        ));
    }
}
