package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackRatTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void setupMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Power and toughness equal the number of Rats you control, counting itself")
    void powerToughnessCountsRats() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);

        harness.addToBattlefieldAndReturn(player1, new PackRat());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rats controlled by an opponent are not counted")
    void opponentRatsDoNotCount() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.addToBattlefieldAndReturn(player2, new PackRat());

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
    }

    @Test
    @DisplayName("{2}{B}, Discard a card creates a token copy that grows the whole pack")
    void activationCreatesTokenCopy() {
        setupMainPhase();
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        setupMainPhase();
        harness.addToBattlefieldAndReturn(player1, new PackRat());
        harness.setHand(player1, new ArrayList<>());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
