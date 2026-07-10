package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "355")
@CardRegistration(set = "9ED", collectorNumber = "322")
public class LlanowarWastes extends Card {

    public LlanowarWastes() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {T}: Add {B}. Llanowar Wastes deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {B}. Llanowar Wastes deals 1 damage to you."
        ));
        // {T}: Add {G}. Llanowar Wastes deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {G}. Llanowar Wastes deals 1 damage to you."
        ));
    }
}
