package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "377")
@CardRegistration(set = "MB2", collectorNumber = "614")
public class Madlands extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds under collector number 377. */
    static {
        Card.registerOracle("Madlands", new OracleData(
                "Madlands",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(CardColor.BLACK, CardColor.RED),
                Set.of(),
                List.of(),
                "Madlands enters the battlefield tapped.\n"
                        + "{T}: Add {B} or {R}.\n"
                        + "Madness {0} (If you discard this card, discard it into exile. When you do, play it for its "
                        + "madness cost or put it into your graveyard. You can play a land only during your turn and "
                        + "only if you have an available land play remaining.)",
                null,
                null,
                Set.of(Keyword.MADNESS),
                null,
                null,
                null));
    }

    public Madlands() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."));
        addCastingOption(new MadnessCast("{0}"));
    }
}
