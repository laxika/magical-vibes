package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillActivePlayerAndCreateTokensByManaValueEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "68")
public class InfernalGenesis extends Card {

    public InfernalGenesis() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new MillActivePlayerAndCreateTokensByManaValueEffect(
                        new CreateTokenEffect("Minion", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.MINION), Set.of(), Set.of())));
    }
}
