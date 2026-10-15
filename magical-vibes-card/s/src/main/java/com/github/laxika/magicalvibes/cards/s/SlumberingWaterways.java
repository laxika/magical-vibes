package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "381")
@CardRegistration(set = "MB2", collectorNumber = "618")
public class SlumberingWaterways extends Card {

    /* The MB2 playtest printing is absent from the public oracle feed under collector number 381. */
    static {
        Card.registerOracle("SlumberingWaterways", new OracleData(
                "Slumbering Waterways",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.GREEN, CardColor.BLUE),
                Set.of(),
                List.of(),
                "Slumbering Waterways enters the battlefield tapped.\n"
                        + "{T}: Add {G} or {U}.\n"
                        + "Flying, vigilance, trample",
                null,
                null,
                Set.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.TRAMPLE),
                null,
                null,
                null));
    }

    public SlumberingWaterways() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE))),
                "{T}: Add {G} or {U}."));
    }
}
