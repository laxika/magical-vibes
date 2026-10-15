package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "146")
@CardRegistration(set = "DMU", collectorNumber = "291")
public class SqueeDubiousMonarch extends Card {

    public SqueeDubiousMonarch() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                1, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), true));

        addCastingOption(new GraveyardCast(null, "{3}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 4))));
    }
}
