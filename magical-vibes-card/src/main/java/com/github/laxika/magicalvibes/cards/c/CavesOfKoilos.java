package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "241")
@CardRegistration(set = "10E", collectorNumber = "350")
@CardRegistration(set = "9ED", collectorNumber = "320")
@CardRegistration(set = "ORI", collectorNumber = "245")
public class CavesOfKoilos extends Card {

    public CavesOfKoilos() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {W}. Caves of Koilos deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {W}. Caves of Koilos deals 1 damage to you."
        ));
        // {T}: Add {B}. Caves of Koilos deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {B}. Caves of Koilos deals 1 damage to you."
        ));
    }
}
