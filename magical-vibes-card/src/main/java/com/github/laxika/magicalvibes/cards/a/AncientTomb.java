package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "315")
public class AncientTomb extends Card {

    public AncientTomb() {
        // {T}: Add {C}{C}. This land deals 2 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2), new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)),
                "{T}: Add {C}{C}. This land deals 2 damage to you."
        ));
    }
}
