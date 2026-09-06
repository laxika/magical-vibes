package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningRift.class, Censor.class, GrizzlyBears.class})
class LightningRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling and paying {1} deals 2 damage to a player")
    void payingDealsDamageToPlayer() {
        prepareGame();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        cycleCensor();
        chooseTarget(player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cycling by an opponent also triggers Lightning Rift")
    void opponentCyclingTriggersAbility() {
        harness.addToBattlefield(player1, new LightningRift());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        chooseTarget(player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cycling and paying {1} deals 2 damage to a creature")
    void payingDealsDamageToCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareGame();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        cycleCensor();
        chooseTarget(creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the payment prevents the damage")
    void decliningPreventsDamage() {
        prepareGame();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        cycleCensor();
        chooseTarget(player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void prepareGame() {
        harness.addToBattlefield(player1, new LightningRift());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void cycleCensor() {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    private void chooseTarget(UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
