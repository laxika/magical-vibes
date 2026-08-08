package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "56")
public class BlazeCommando extends Card {

    public BlazeCommando() {
        // "Whenever an instant or sorcery spell you control deals damage, create two 1/1 red and
        // white Soldier creature tokens with haste." Fires once per damage event regardless of how
        // many objects the spell damaged simultaneously.
        addEffect(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE, new CreateTokenEffect(
                2, "Soldier", 1, 1,
                CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                List.of(CardSubtype.SOLDIER), Set.of(Keyword.HASTE), Set.of()));
    }
}
