package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextSpellOfTypesThisTurnEffect;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "202")
public class MaelstromMuse extends Card {

    public MaelstromMuse() {
        addEffect(EffectSlot.ON_ATTACK, new ReduceCastCostForNextSpellOfTypesThisTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY), new SourcePower()));
    }
}
