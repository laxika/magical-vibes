package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "614")
public class WallOff extends Card {

    public WallOff() {
        // This spell costs {1} less to cast for each creature your opponents control.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS)));

        // Create a 0/4 colorless Wall creature token with defender. You gain 4 life.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Wall", 0, 4, null,
                List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.<CardType>of()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
