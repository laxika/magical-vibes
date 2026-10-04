package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DregRecycler.class, GrizzlyBears.class, LeoninScimitar.class})
class DregRecyclerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature drains each opponent for 1 and gains 1 life")
    void sacrificingCreatureDrainsOpponent() {
        Permanent recycler = addReadyRecycler(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(recycler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing an artifact drains each opponent")
    void sacrificingArtifactDrainsOpponent() {
        addReadyRecycler(player1);
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, scimitar.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Dreg Recycler can sacrifice itself")
    void canSacrificeItself() {
        addReadyRecycler(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Dreg Recycler");
    }

    @Test
    @DisplayName("Sacrifice is paid before the life changes resolve")
    void sacrificeIsPaidBeforeResolution() {
        addReadyRecycler(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Dreg Recycler");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A summoning-sick Recycler cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent recycler = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        recycler.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Dreg Recycler");
        assertThat(recycler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Recycler cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent recycler = addReadyRecycler(player1);
        recycler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Dreg Recycler");
    }

    private Permanent addReadyRecycler(Player player) {
        Permanent recycler = harness.addToBattlefieldAndReturn(player, new DregRecycler());
        recycler.setSummoningSick(false);
        return recycler;
    }
}
