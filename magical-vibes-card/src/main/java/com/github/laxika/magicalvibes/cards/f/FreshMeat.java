package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "109")
public class FreshMeat extends Card {

    public FreshMeat() {
        // Create a 3/3 green Beast creature token for each creature put into your
        // graveyard from the battlefield this turn.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new CreatureDeathsThisTurn(CountScope.CONTROLLER),
                "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST),
                Set.of(), Set.of()
        ));
    }
}
