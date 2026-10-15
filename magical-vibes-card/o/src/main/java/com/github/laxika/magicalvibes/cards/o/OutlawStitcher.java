package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "59")
public class OutlawStitcher extends Card {

    public OutlawStitcher() {
        var countersForSpellsAfterFirst = new Scaled(
                new Max(new Fixed(0),
                        new Sum(new SpellsCastThisTurn(CountScope.CONTROLLER), new Fixed(-1))), 2);
        var zombieRogue = new CreateTokenEffect(
                CardType.CREATURE, 1, "Zombie Rogue", 2, 2,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.BLACK),
                List.of(CardSubtype.ZOMBIE, CardSubtype.ROGUE), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, countersForSpellsAfterFirst)),
                List.of(), false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, zombieRogue);
    }
}
