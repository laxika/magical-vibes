package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkkiAvalanchers.class, Forest.class})
class AkkiAvalanchersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives +2/+0")
    void sacrificeLandBoosts() {
        Permanent akki = harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(akki.getEffectivePower()).isEqualTo(3);
        assertThat(akki.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller chooses which land to sacrifice")
    void choosesLandToSacrifice() {
        Permanent akki = harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondForest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(akki.getId(), firstForest.getId());
        assertThat(akki.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent akki = harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(akki.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated without a land to sacrifice")
    void requiresLand() {
        harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefieldAndReturn(player1, new AkkiAvalanchers());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(opponentForest.getId());
    }
}
