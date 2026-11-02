package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextSpellOfTypesThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "202")
public class MaelstromMuse extends Card {

    public MaelstromMuse() {
        addTrigger(Triggers.attacks(), new ReduceCastCostForNextSpellOfTypesThisTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY), new SourcePower()));
    }
}
