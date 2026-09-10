package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JinxedIdol.class, TrainedArmodon.class})
class JinxedIdolTest extends BaseCardTest {

    private void addIdolAndCreature(Player player) {
        harness.addToBattlefield(player, new JinxedIdol());
        harness.addToBattlefield(player, new TrainedArmodon());
    }

    @Test
    @DisplayName("Jinxed Idol deals 2 damage to controller during upkeep")
    void upkeepDealsDamageToController() {
        harness.addToBattlefield(player1, new JinxedIdol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Jinxed Idol does not trigger during non-controller upkeep")
    void doesNotTriggerDuringNonControllerUpkeep() {
        harness.addToBattlefield(player1, new JinxedIdol());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Sacrifice a creature to give Jinxed Idol to opponent")
    void sacrificeCreatureGivesIdolToOpponent() {
        addIdolAndCreature(player1);

        // Activate ability: sacrifice creature, target opponent
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities(); // resolve ability

        // Trained Armodon should be sacrificed
        harness.assertInGraveyard(player1, "Trained Armodon");

        // Jinxed Idol should now be on player2's battlefield
        harness.assertOnBattlefield(player2, "Jinxed Idol");
        harness.assertNotOnBattlefield(player1, "Jinxed Idol");
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

        harness.assertLife(player2, p2LifeBefore - 2);
        harness.assertLife(player1, p1LifeBefore);
    }

    @Test
    @DisplayName("Cannot activate ability without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new JinxedIdol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the controller with the control-transfer ability")
    void cannotTargetController() {
        addIdolAndCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Jinxed Idol");
        harness.assertOnBattlefield(player1, "Trained Armodon");
        harness.assertNotInGraveyard(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("Player chooses which creature to sacrifice when multiple are available")
    void choosesCreatureToSacrificeWithMultiple() {
        harness.addToBattlefield(player1, new JinxedIdol());
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.addToBattlefield(player1, new TrainedArmodon());

        UUID firstArmodonId = harness.getPermanentId(player1, "Trained Armodon");

        // With 2 creatures, the system prompts for a choice
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, firstArmodonId);
        harness.passBothPriorities();

        // Idol should be on player2's battlefield
        harness.assertOnBattlefield(player2, "Jinxed Idol");
        // One Trained Armodon should remain
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Trained Armodon"))
                .hasSize(1);
    }
}
