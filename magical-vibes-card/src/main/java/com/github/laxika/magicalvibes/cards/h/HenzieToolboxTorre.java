package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.GrantBlitzToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "2")
@CardRegistration(set = "NCC", collectorNumber = "102")
@CardRegistration(set = "NCC", collectorNumber = "187")
public class HenzieToolboxTorre extends Card {

    public HenzieToolboxTorre() {
        addEffect(EffectSlot.STATIC, new GrantBlitzToSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMinManaValuePredicate(4))),
                new CommanderCastsFromCommandZoneThisGame()));
    }
}
