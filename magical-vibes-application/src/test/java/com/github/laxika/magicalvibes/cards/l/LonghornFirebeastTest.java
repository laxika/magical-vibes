package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LonghornFirebeast.class, FieryTemper.class})
class LonghornFirebeastTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.setHand(player1, List.of(new LonghornFirebeast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent declines — Firebeast stays and no damage is dealt")
    void decliningKeepsFirebeast() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Longhorn Firebeast");
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Longhorn Firebeast");
    }

    @Test
    @DisplayName("Opponent accepts — takes 5 damage and Firebeast is sacrificed")
    void acceptingDamagesAndSacrifices() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Longhorn Firebeast");
        harness.assertInGraveyard(player1, "Longhorn Firebeast");
    }

    @Test
    @DisplayName("Opponent accepts even when the damage is prevented, so Firebeast is sacrificed")
    void acceptingStillSacrificesWhenDamageIsPrevented() {
        castAndResolveToChoice();
        gd.playersWithAllDamagePrevented.add(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Longhorn Firebeast");
        harness.assertInGraveyard(player1, "Longhorn Firebeast");
    }

    @Test
    @DisplayName("The trigger still deals damage after Firebeast is destroyed in response")
    void acceptingDealsDamageAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of(new LonghornFirebeast()));
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Longhorn Firebeast"));

        harness.assertNotOnBattlefield(player1, "Longhorn Firebeast");
        harness.assertInGraveyard(player1, "Longhorn Firebeast");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LonghornFirebeast)
                .hasSize(1);
    }
}
