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

@CardRegistration(set = "MRD", collectorNumber = "253")
@CardRegistration(set = "SLD", collectorNumber = "1053")
@CardRegistration(set = "AA1", collectorNumber = "20")
@CardRegistration(set = "WHO", collectorNumber = "841")
@CardRegistration(set = "WHO", collectorNumber = "250")
@CardRegistration(set = "PIP", collectorNumber = "246")
@CardRegistration(set = "PIP", collectorNumber = "774")
@CardRegistration(set = "40K", collectorNumber = "254")
@CardRegistration(set = "40K", collectorNumber = "255")
@CardRegistration(set = "MSC", collectorNumber = "219")
@CardRegistration(set = "M3C", collectorNumber = "310")
@CardRegistration(set = "MKC", collectorNumber = "242")
@CardRegistration(set = "MIC", collectorNumber = "164")
@CardRegistration(set = "WOC", collectorNumber = "150")
@CardRegistration(set = "FIC", collectorNumber = "364")
@CardRegistration(set = "DRC", collectorNumber = "141")
@CardRegistration(set = "SCD", collectorNumber = "280")
public class TalismanOfDominance extends Card {

    public TalismanOfDominance() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {U} or {B}. This artifact deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK)),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{T}: Add {U} or {B}. This artifact deals 1 damage to you."
        ));
    }
}
