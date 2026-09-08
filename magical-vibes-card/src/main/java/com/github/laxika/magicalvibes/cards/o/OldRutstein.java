package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndCreateTokensByMilledCardTypeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "244")
public class OldRutstein extends Card {

    private static final MillControllerAndCreateTokensByMilledCardTypeEffect MILL_AND_CREATE_TOKENS =
            new MillControllerAndCreateTokensByMilledCardTypeEffect(
                    CreateTokenEffect.ofTreasureToken(1),
                    new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                            List.of(CardSubtype.INSECT), Set.of(), Set.of()),
                    CreateTokenEffect.ofBloodToken(1));

    public OldRutstein() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, MILL_AND_CREATE_TOKENS);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, MILL_AND_CREATE_TOKENS);
    }
}
