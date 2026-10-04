package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.l.LethalExploit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("Gravelighter")
@CardUsed({Gravelighter.class, Forest.class, JukaiTrainee.class, LethalExploit.class})
class GravelighterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature died this turn")
    void drawsWhenCreatureDiedThisTurn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new JukaiTrainee());
        harness.addToBattlefield(player2, new JukaiTrainee());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravelighter");
        harness.assertOnBattlefield(player1, "Jukai Trainee");
        harness.assertOnBattlefield(player2, "Jukai Trainee");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Otherwise makes each player sacrifice a creature")
    void otherwiseEachPlayerSacrificesCreature() {
        harness.addToBattlefield(player2, new JukaiTrainee());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gravelighter");
        harness.assertInGraveyard(player2, "Jukai Trainee");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new Gravelighter(), "{2}{B}");
    }

    @Test
    @DisplayName("Checks for deaths at resolution even if Gravelighter itself dies in response")
    void drawsWhenSourceDiesInResponse() {
        harness.setLibrary(player1, List.of(new Forest()));
        setupAndCast();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LethalExploit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gravelighter"));
        harness.assertInGraveyard(player1, "Gravelighter");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("All players choose before any creature is sacrificed")
    void sacrificesOnlyAfterAllChoicesAreMade() {
        harness.addToBattlefield(player1, new JukaiTrainee());
        harness.addToBattlefield(player2, new JukaiTrainee());
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jukai Trainee");
        harness.assertOnBattlefield(player2, "Jukai Trainee");
        harness.assertNotInGraveyard(player2, "Jukai Trainee");

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Jukai Trainee"));
        harness.assertInGraveyard(player1, "Jukai Trainee");
        harness.assertInGraveyard(player2, "Jukai Trainee");
        harness.assertOnBattlefield(player1, "Gravelighter");
    }

    @Test
    @DisplayName("Sacrifices itself even when the opponent controls no creatures")
    void sacrificesItselfWithEmptyOpposingBattlefield() {
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gravelighter");
    }
}
