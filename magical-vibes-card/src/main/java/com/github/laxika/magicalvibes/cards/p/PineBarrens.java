package com.github.laxika.magicalvibes.cards.p;

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

@CardRegistration(set = "TMP", collectorNumber = "321")
@CardRegistration(set = "TPR", collectorNumber = "240")
public class PineBarrens extends Card {

    public PineBarrens() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {B}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {B}. Pine Barrens deals 1 damage to you."
        ));
        // {T}: Add {G}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {G}. Pine Barrens deals 1 damage to you."
        ));
    }
}
