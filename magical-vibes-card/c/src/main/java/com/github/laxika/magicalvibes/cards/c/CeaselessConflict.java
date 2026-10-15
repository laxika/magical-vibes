package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyedPermanentCountScope;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "12")
@CardRegistration(set = "SOC", collectorNumber = "62")
public class CeaselessConflict extends Card {

    public CeaselessConflict() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(),
                false,
                EachPermanentScope.ALL_PLAYERS,
                new CreateTokenEffect(CardType.CREATURE, new EventValue(), "Spirit", 3, 2,
                        CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT),
                        Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of()),
                false,
                DestroyedPermanentCountScope.CONTROLLER_NONTOKEN));
    }
}
