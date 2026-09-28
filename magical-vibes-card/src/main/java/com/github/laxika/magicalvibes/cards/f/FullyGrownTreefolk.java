package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

public class FullyGrownTreefolk extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("FullyGrownTreefolk", new OracleData(
                "Fully-Grown Treefolk",
                CardType.CREATURE,
                Set.of(),
                "{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.TREEFOLK),
                "Fully-Grown Treefolk's power and toughness are each equal to the number of lands you control.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public FullyGrownTreefolk() {
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));
    }
}
