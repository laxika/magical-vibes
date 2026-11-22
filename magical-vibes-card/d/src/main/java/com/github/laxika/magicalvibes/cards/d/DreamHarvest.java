package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ECL", collectorNumber = "216")
@CardRegistration(set = "ECL", collectorNumber = "371")
public class DreamHarvest extends Card {

    public DreamHarvest() {
        addSpellEffect(SequenceEffect.of(
                ExileCardsFromTopUntilEffect.untilTotalManaValue(Subjects.eachOpponent(), 5),
                AllowPlayCardsFromExileEffect.thisTurn(
                                Subjects.cardsThisWay(StepKind.EXILE, ExileCardsFromTopUntilEffect.NONLAND))
                        .withoutPayingManaCost()));
    }
}
