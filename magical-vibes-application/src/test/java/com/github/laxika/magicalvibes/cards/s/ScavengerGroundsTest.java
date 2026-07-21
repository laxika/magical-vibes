package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraspingDunes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScavengerGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyGrounds(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{2}, {T}, Sacrifice a Desert: exiles all graveyards")
    void exileAbilityClearsAllGraveyards() {
        Permanent grounds = addReadyGrounds(player1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(grounds.getId()));
        // Sacrificed as cost into GY, then exiled when the ability resolves.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scavenger Grounds"));
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent otherDesert = new Permanent(new GraspingDunes());
        otherDesert.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherDesert);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(grounds.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    private Permanent addReadyGrounds(Player player) {
        Permanent perm = new Permanent(new ScavengerGrounds());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
