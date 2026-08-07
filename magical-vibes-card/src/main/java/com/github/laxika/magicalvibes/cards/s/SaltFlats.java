package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "324")
public class SaltFlats extends Card {

    public SaltFlats() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {W}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {W}. Salt Flats deals 1 damage to you."
        ));
        // {T}: Add {B}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {B}. Salt Flats deals 1 damage to you."
        ));
    }
}
