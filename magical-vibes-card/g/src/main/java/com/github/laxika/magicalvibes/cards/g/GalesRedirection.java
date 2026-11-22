package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HBG", collectorNumber = "119")
public class GalesRedirection extends Card {

    public GalesRedirection() {
        addSpellEffect(new RollD20Effect(
                null,
                SequenceEffect.of(
                        new ExileTargetSpellEffect(),
                        AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.EXILE))
                                .withAnyManaType()),
                SequenceEffect.of(
                        new ExileTargetSpellEffect(),
                        AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.EXILE))
                                .withoutPayingManaCost()),
                null,
                new Scaled(new TargetSpellManaValue(), -1),
                14,
                false));
    }
}
