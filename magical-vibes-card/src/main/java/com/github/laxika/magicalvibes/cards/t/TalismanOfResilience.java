package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1059")
@CardRegistration(set = "SLD", collectorNumber = "2113")
@CardRegistration(set = "AA1", collectorNumber = "25")
@CardRegistration(set = "MH1", collectorNumber = "234")
@CardRegistration(set = "PIP", collectorNumber = "250")
@CardRegistration(set = "PIP", collectorNumber = "778")
@CardRegistration(set = "C21", collectorNumber = "270")
@CardRegistration(set = "DSC", collectorNumber = "255")
@CardRegistration(set = "M3C", collectorNumber = "314")
public class TalismanOfResilience extends Card {

    public TalismanOfResilience() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {B} or {G}. This artifact deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN)),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{T}: Add {B} or {G}. This artifact deals 1 damage to you."
        ));
    }
}
