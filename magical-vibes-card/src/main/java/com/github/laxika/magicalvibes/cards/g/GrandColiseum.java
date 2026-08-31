package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "319")
public class GrandColiseum extends Card {

    public GrandColiseum() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add one mana of any color. This land deals 1 damage to you."
        ));
    }
}
