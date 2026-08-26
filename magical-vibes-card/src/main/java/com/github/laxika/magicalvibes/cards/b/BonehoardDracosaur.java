package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnAndCreateTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "134")
@CardRegistration(set = "LCI", collectorNumber = "321")
public class BonehoardDracosaur extends Card {

    public BonehoardDracosaur() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardsMayPlayThisTurnAndCreateTokensEffect(
                        new CreateTokenEffect("Dinosaur", 3, 1, CardColor.RED,
                                List.of(CardSubtype.DINOSAUR), Set.of(), Set.of()),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
