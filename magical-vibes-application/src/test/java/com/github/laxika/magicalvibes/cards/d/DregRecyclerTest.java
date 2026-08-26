package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyRecycler(Player player) {
        Permanent recycler = new Permanent(new DregRecycler());
        recycler.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(recycler);
        return recycler;
    }
}
