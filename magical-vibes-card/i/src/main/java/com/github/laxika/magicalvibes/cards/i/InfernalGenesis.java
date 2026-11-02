package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillActivePlayerAndCreateTokensByManaValueEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "68")
public class InfernalGenesis extends Card {

    public InfernalGenesis() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new MillActivePlayerAndCreateTokensByManaValueEffect(
                        new CreateTokenEffect("Minion", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.MINION), Set.of(), Set.of())));
    }
}
