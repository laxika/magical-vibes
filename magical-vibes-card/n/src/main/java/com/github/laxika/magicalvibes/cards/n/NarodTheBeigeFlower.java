package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "360")
@CardRegistration(set = "MB2", collectorNumber = "599")
public class NarodTheBeigeFlower extends Card {

    static {
        Card.registerOracle("NarodTheBeigeFlower", new OracleData(
                "Narod, the Beige Flower",
                CardType.CREATURE,
                Set.of(),
                "{W}{U}{B}",
                CardColor.WHITE,
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK),
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.PLANT, CardSubtype.ROGUE),
                "Each creature assigns combat damage equal to its mana value rather than its power.",
                0,
                5,
                Set.of(),
                null,
                null,
                null));
    }

    public NarodTheBeigeFlower() {
        addEffect(EffectSlot.STATIC,
                new AssignCombatDamageWithManaValueEffect(GrantScope.ALL_CREATURES));
    }
}
