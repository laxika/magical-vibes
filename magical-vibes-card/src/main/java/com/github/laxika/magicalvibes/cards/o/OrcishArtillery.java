package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "220")
@CardRegistration(set = "9ED", collectorNumber = "206")
@CardRegistration(set = "8ED", collectorNumber = "207")
@CardRegistration(set = "7ED", collectorNumber = "205")
@CardRegistration(set = "6ED", collectorNumber = "196")
public class OrcishArtillery extends Card {

    public OrcishArtillery() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(2), new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)),
                "{T}: Orcish Artillery deals 2 damage to any target and 3 damage to you."
        ));
    }
}
