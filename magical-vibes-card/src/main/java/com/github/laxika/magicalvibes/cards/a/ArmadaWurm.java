package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "143")
public class ArmadaWurm extends Card {

    public ArmadaWurm() {
        // When this creature enters, create a 5/5 green Wurm creature token with trample.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Wurm", 5, 5, CardColor.GREEN,
                List.of(CardSubtype.WURM), Set.of(Keyword.TRAMPLE), Set.of()
        ));
    }
}
