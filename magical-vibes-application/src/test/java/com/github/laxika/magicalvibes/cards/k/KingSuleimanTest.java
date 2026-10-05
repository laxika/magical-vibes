package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarmattanEfreet;
import com.github.laxika.magicalvibes.cards.n.NettletoothDjinn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KingSuleiman.class, NettletoothDjinn.class, HarmattanEfreet.class, GrizzlyBears.class})
class KingSuleimanTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability destroys a target Djinn")
    void destroysDjinn() {
        Permanent king = addReadyKing();
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new NettletoothDjinn());

        harness.activateAbility(player1, 0, null, djinn.getId());
        harness.passBothPriorities();

        assertThat(king.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Nettletooth Djinn");
        harness.assertInGraveyard(player2, "Nettletooth Djinn");
    }

    @Test
    @DisplayName("Tap ability destroys a target Efreet")
    void destroysEfreet() {
        addReadyKing();
        Permanent efreet = harness.addToBattlefieldAndReturn(player2, new HarmattanEfreet());

        harness.activateAbility(player1, 0, null, efreet.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Harmattan Efreet");
        harness.assertInGraveyard(player2, "Harmattan Efreet");
    }

    @Test
    @DisplayName("Tap ability cannot target another creature type")
    void cannotTargetAnotherCreatureType() {
        addReadyKing();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability can destroy its controller's Djinn")
    void destroysOwnDjinn() {
        addReadyKing();
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new NettletoothDjinn());

        harness.activateAbility(player1, 0, null, djinn.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nettletooth Djinn");
        harness.assertInGraveyard(player1, "Nettletooth Djinn");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new KingSuleiman());
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new NettletoothDjinn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, djinn.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(king.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Nettletooth Djinn");
    }

    @Test
    @DisplayName("An already tapped King cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent king = addReadyKing();
        king.tap();
        Permanent efreet = harness.addToBattlefieldAndReturn(player2, new HarmattanEfreet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, efreet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player2, "Harmattan Efreet");
    }

    @Test
    @DisplayName("A target that loses its creature types is not destroyed")
    void doesNotDestroyTargetThatLosesCreatureTypes() {
        Permanent king = addReadyKing();
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new NettletoothDjinn());

        harness.activateAbility(player1, 0, null, djinn.getId());
        djinn.setLosesAllCreatureTypesUntilEndOfTurn(true);
        harness.passBothPriorities();

        assertThat(king.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Nettletooth Djinn");
        harness.assertNotInGraveyard(player2, "Nettletooth Djinn");
    }

    private Permanent addReadyKing() {
        return addCreatureReady(player1, new KingSuleiman());
    }
}
