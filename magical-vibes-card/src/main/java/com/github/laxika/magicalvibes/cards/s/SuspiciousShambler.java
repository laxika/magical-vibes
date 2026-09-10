package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "527")
public class SuspiciousShambler extends Card {

    public SuspiciousShambler() {
        // {4}{B}{B}, Exile this card from your graveyard: Create two 2/2 black Zombie creature
        // tokens. Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        CreateTokenEffect.blackZombie(2)
                ),
                "{4}{B}{B}, Exile this card from your graveyard: Create two 2/2 black Zombie "
                        + "creature tokens. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
