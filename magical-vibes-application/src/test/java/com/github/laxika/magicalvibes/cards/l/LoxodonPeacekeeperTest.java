package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoxodonPeacekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Player with the lowest life total gains control on upkeep")
    void lowestLifePlayerGainsControl() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(peacekeeper);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(peacekeeper);
    }

    @Test
    @DisplayName("Controller chooses which tied lowest-life player gains control")
    void controllerChoosesOnLowestLifeTie() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(peacekeeper);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(peacekeeper);
    }

    @Test
    @DisplayName("Upkeep trigger only fires during the source controller's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.stack).isEmpty();
    }
}
