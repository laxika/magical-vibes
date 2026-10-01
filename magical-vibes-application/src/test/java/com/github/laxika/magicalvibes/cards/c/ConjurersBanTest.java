package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.cards.s.SkarrgTheRagePits;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConjurersBan.class, Quicken.class, SkarrgTheRagePits.class})
class ConjurersBanTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a name draws a card and prevents that spell for every player")
    void preventsChosenSpellForEveryPlayer() {
        harness.setHand(player2, List.of(new Quicken()));
        harness.setLibrary(player1, List.of(new SkarrgTheRagePits()));

        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Quicken");

        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName())
                .contains("Skarrg, the Rage Pits");

        harness.setHand(player1, List.of(new Quicken()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new Quicken()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing a land name prevents that land from being played for every player")
    void preventsChosenLandForEveryPlayer() {
        harness.setHand(player2, List.of(new SkarrgTheRagePits()));
        harness.setLibrary(player1, List.of(new Quicken()));

        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Skarrg, the Rage Pits");

        harness.setHand(player1, List.of(new SkarrgTheRagePits()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new SkarrgTheRagePits()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction ends at the beginning of the controller's next turn")
    void restrictionEndsAtControllersNextTurn() {
        harness.setHand(player2, List.of(new Quicken()));

        harness.castFromHand(player1, new ConjurersBan(), "{W}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Quicken");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        harness.setHand(player1, List.of(new Quicken()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(c -> c.getName())
                .contains("Quicken");

        harness.setHand(player1, List.of(new SkarrgTheRagePits()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .contains("Skarrg, the Rage Pits");
    }
}
