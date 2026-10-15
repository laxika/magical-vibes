package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "382")
@CardRegistration(set = "MB2", collectorNumber = "383")
@CardRegistration(set = "MB2", collectorNumber = "619")
public class TemurElevator extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds under collector number 382. */
    static {
        Card.registerOracle("TemurElevator", new OracleData(
                "Temur Elevator",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.GREEN, CardColor.RED, CardColor.BLUE),
                Set.of(),
                List.of(),
                "Ascend (If you control ten or more permanents, you get the city's blessing for the rest of the game.)\n"
                        + "{T}: Add {G}, {U}, or {R}. If you don't have the city's blessing, you lose 1 life.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public TemurElevator() {
        addEffect(EffectSlot.STATIC, new AscendEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)),
                        new ConditionalEffect(
                                new NotCondition(new ControllerHasCityBlessing()),
                                new LoseLifeEffect(1))),
                "{T}: Add {G}, {U}, or {R}. If you don't have the city's blessing, you lose 1 life."
        ));
    }
}
