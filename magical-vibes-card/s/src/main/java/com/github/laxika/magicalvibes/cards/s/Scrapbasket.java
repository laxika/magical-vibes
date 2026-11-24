package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.EnumSet;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;

@CardRegistration(set = "SHM", collectorNumber = "262")
public class Scrapbasket extends Card {

    public Scrapbasket() {
        // {1}: This creature becomes all colors until end of turn (self-scoped layer-5 color set).
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(BecomeChosenColorsUntilEndOfTurnEffect.fixed(
                EnumSet.allOf(CardColor.class), Subjects.source(), Duration.UNTIL_END_OF_TURN)),
                "{1}: This creature becomes all colors until end of turn."));
    }
}
