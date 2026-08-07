package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BudokaGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a land from hand onto the battlefield untapped")
    void putsLandOntoBattlefield() {
        addReadyGardener(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not flip while fewer than ten lands are controlled")
    void staysUnflippedBelowTenLands() {
        Permanent gardener = addReadyGardener(player1);
        addForests(player1, 8);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gardener.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips when the land put onto the battlefield is the tenth")
    void flipsWhenPutLandIsTheTenth() {
        Permanent gardener = addReadyGardener(player1);
        addForests(player1, 9);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gardener.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips on the land-count check even when the land put is declined")
    void flipsAfterDecliningWhenAlreadyAtTenLands() {
        Permanent gardener = addReadyGardener(player1);
        addForests(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gardener.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Only lands the controller owns count toward ten")
    void opponentLandsDoNotCount() {
        Permanent gardener = addReadyGardener(player1);
        addForests(player1, 5);
        addForests(player2, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gardener.isTransformed()).isFalse();
    }

    private Permanent addReadyGardener(Player player) {
        Permanent gardener = new Permanent(new BudokaGardener());
        gardener.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(gardener);
        return gardener;
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            gd.playerBattlefields.get(player.getId()).add(new Permanent(new Forest()));
        }
    }
}
