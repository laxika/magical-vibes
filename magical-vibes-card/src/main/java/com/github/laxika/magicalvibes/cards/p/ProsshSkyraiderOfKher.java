package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "A25", collectorNumber = "214")
@CardRegistration(set = "C13", collectorNumber = "204")
public class ProsshSkyraiderOfKher extends Card {

    public ProsshSkyraiderOfKher() {
        // When you cast this spell, create X 0/1 red Kobold creature tokens named Kobolds of Kher Keep,
        // where X is the amount of mana spent to cast it.
        addEffect(EffectSlot.ON_SELF_CAST, new CreateTokenEffect(
                new ManaSpentToCast(), "Kobolds of Kher Keep", 0, 1, CardColor.RED,
                List.of(CardSubtype.KOBOLD), Set.of(), Set.of()));

        // Sacrifice another creature: Prossh gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new BoostSelfEffect(1, 0)
                ),
                "Sacrifice another creature: Prossh, Skyraider of Kher gets +1/+0 until end of turn."
        ));
    }
}
