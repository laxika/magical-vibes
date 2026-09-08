package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "84")
public class PenumbraWurm extends Card {

    public PenumbraWurm() {
        // When this creature dies, create a 6/6 black Wurm creature token with trample.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Wurm", 6, 6, CardColor.BLACK,
                List.of(CardSubtype.WURM), Set.of(Keyword.TRAMPLE), Set.of()));
    }
}
