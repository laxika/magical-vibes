package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.CompellingArgument;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ZenithFlare.class, Censor.class, CompellingArgument.class, GrizzlyBears.class})
class ZenithFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and gains life equal to cycling cards in the controller's graveyard")
    void dealsDamageAndGainsLifeForCyclingCards() {
        harness.setGraveyard(player1, List.of(new Censor(), new CompellingArgument(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counts only the controller's cycling cards at resolution")
    void countsOnlyControllerCyclingCardsAtResolution() {
        harness.setGraveyard(player1, List.of(new Censor(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new CompellingArgument()));
        harness.setHand(player1, List.of(new ZenithFlare()));
        addZenithFlareMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    private void addZenithFlareMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
