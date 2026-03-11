package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JinxedIdolTest extends BaseCardTest {

    private void addIdolAndCreature(Player player) {
        harness.addToBattlefield(player, new JinxedIdol());
        harness.addToBattlefield(player, new GrizzlyBears());
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("Jinxed Idol deals 2 damage to controller during upkeep")
    void upkeepDealsDamageToController() {
        harness.addToBattlefield(player1, new JinxedIdol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Jinxed Idol does not trigger during non-controller upkeep")
    void doesNotTriggerDuringNonControllerUpkeep() {
        harness.addToBattlefield(player1, new JinxedIdol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Sacrifice a creature to give Jinxed Idol to opponent")
    void sacrificeCreatureGivesIdolToOpponent() {
        addIdolAndCreature(player1);

        // Activate ability: sacrifice creature, target opponent
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities(); // resolve ability

        // Grizzly Bears should be sacrificed
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Jinxed Idol should now be on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Jinxed Idol"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Jinxed Idol"));
    }

    @Test
    @DisplayName("After control change, Jinxed Idol damages new controller on their upkeep")
    void idolDamagesNewControllerAfterTransfer() {
        addIdolAndCreature(player1);

        // Transfer idol to player2
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore);
    }

    @Test
    @DisplayName("Cannot activate ability without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new JinxedIdol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player chooses which creature to sacrifice when multiple are available")
    void choosesCreatureToSacrificeWithMultiple() {
        harness.addToBattlefield(player1, new JinxedIdol());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID firstBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // With 2 creatures, the system prompts for a choice
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, firstBearsId);
        harness.passBothPriorities();

        // Idol should be on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Jinxed Idol"));
        // One Grizzly Bears should remain
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }
}
