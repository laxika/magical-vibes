package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "100")
public class TidalWave extends Card {

    public TidalWave() {
        // Create a 5/5 blue Wall creature token with defender.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Wall", 5, 5, CardColor.BLUE,
                List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.<CardType>of()));

        // Sacrifice it at the beginning of the next end step.
        addEffect(EffectSlot.SPELL, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
