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

@CardRegistration(set = "AA1", collectorNumber = "17")
@CardRegistration(set = "MH1", collectorNumber = "230")
public class TalismanOfConviction extends Card {

    public TalismanOfConviction() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {R} or {W}. This artifact deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE)),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{T}: Add {R} or {W}. This artifact deals 1 damage to you."
        ));
    }
}
