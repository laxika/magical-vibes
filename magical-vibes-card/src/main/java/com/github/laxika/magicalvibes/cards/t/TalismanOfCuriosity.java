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

@CardRegistration(set = "SLD", collectorNumber = "1061")
@CardRegistration(set = "AA1", collectorNumber = "19")
@CardRegistration(set = "MH1", collectorNumber = "232")
@CardRegistration(set = "M3C", collectorNumber = "309")
@CardRegistration(set = "WHO", collectorNumber = "249")
@CardRegistration(set = "MKC", collectorNumber = "241")
@CardRegistration(set = "PIP", collectorNumber = "245")
@CardRegistration(set = "PIP", collectorNumber = "773")
@CardRegistration(set = "WHO", collectorNumber = "840")
public class TalismanOfCuriosity extends Card {

    public TalismanOfCuriosity() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {G} or {U}. This artifact deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE)),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{T}: Add {G} or {U}. This artifact deals 1 damage to you."
        ));
    }
}
