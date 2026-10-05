package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MigratingKetradon.class, GrizzlyBears.class})
class MigratingKetradonTest extends BaseCardTest {

    @Test
    void entersAndGainsFourLife() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertOnBattlefield(player1, "Migrating Ketradon");
    }

    @Test
    void cyclingDiscardsThisCardAndDraws() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Migrating Ketradon");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void lifeGainWaitsForEntryTriggerToResolve() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Migrating Ketradon");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void entryTriggerGainsLifeForTheOtherController() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MigratingKetradon()));
        harness.addMana(player2, ManaColor.GREEN, 6);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 24);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Migrating Ketradon");
    }

    @Test
    void cyclingPaysDiscardBeforeDrawingAndDoesNotGainLife() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.setLibrary(player1, List.of(new MigratingKetradon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Migrating Ketradon");
        harness.assertNotInHand(player1, "Migrating Ketradon");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Migrating Ketradon");
        harness.assertNotOnBattlefield(player1, "Migrating Ketradon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cyclingCannotBeActivatedWithOnlyOneMana() {
        harness.setHand(player1, List.of(new MigratingKetradon()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Migrating Ketradon");
        harness.assertNotInGraveyard(player1, "Migrating Ketradon");
        assertThat(gd.stack).isEmpty();
    }
}
