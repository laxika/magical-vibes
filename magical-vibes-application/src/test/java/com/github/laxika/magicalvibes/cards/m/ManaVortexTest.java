package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaVortex.class, GrizzlyBears.class, Island.class})
class ManaVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Counters itself on cast when its controller controls no land")
    void countersItselfWithoutLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castManaVortex(player1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mana Vortex");
        harness.assertNotOnBattlefield(player1, "Mana Vortex");
    }

    @Test
    @DisplayName("Sacrificing a land lets Mana Vortex resolve")
    void sacrificingLandLetsItResolve() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castManaVortex(player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, island.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mana Vortex");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(island.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Each player's upkeep makes that player sacrifice a land")
    void activePlayerSacrificesLandOnUpkeep() {
        harness.addToBattlefield(player1, new ManaVortex());
        Permanent controllerLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent activePlayerLand = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(activePlayerLand.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(controllerLand.getId()));
        harness.assertOnBattlefield(player1, "Mana Vortex");
    }

    @Test
    @DisplayName("Sacrifices itself after the last land leaves the battlefield")
    void sacrificesItselfWhenThereAreNoLands() {
        harness.addToBattlefield(player1, new ManaVortex());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mana Vortex");
    }

    private void castManaVortex(Player player) {
        harness.setHand(player, List.of(new ManaVortex()));
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player, 0);
    }
}
