package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "158")
public class SummoningStation extends Card {

    public SummoningStation() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect("Pincher", 2, 2, null,
                        List.of(CardSubtype.PINCHER), Set.of(), Set.of())),
                "{T}: Create a 2/2 colorless Pincher creature token."
        ));

        addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF),
                        "Untap Summoning Station?"));
    }
}
