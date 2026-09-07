package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "280")
public class UrzasFactory extends Card {

    public UrzasFactory() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new CreateTokenEffect(
                        "Assembly-Worker", 2, 2, null,
                        List.of(CardSubtype.ASSEMBLY_WORKER), Set.of(), Set.of(CardType.ARTIFACT)
                )),
                "{7}, {T}: Create a 2/2 colorless Assembly-Worker artifact creature token."
        ));
    }
}
