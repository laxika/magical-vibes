package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonPeacekeeper.class, MomentaryBlink.class})
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
    @DisplayName("Uses life totals when the upkeep trigger resolves")
    void usesLifeTotalsAtResolution() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
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

    @Test
    @DisplayName("Lowest-life controller retains control without becoming summoning sick")
    void lowestLifeControllerRetainsControl() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(peacekeeper);
        assertThat(peacekeeper.isSummoningSick()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Tied controller can choose to retain control")
    void tiedControllerCanChooseItself() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(peacekeeper);
        assertThat(peacekeeper.isSummoningSick()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("New controller's upkeep can transfer control back to the owner")
    void triggersDuringNewControllersUpkeep() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(peacekeeper);
        assertThat(peacekeeper.isSummoningSick()).isTrue();

        harness.setLife(player1, 5);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(peacekeeper);
        assertThat(peacekeeper.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Pending upkeep trigger cannot take control of a blinked Peacekeeper")
    void pendingTriggerDoesNotAffectReturnedPermanent() {
        Permanent peacekeeper = addCreatureReady(player1, new LoxodonPeacekeeper());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, peacekeeper.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Loxodon Peacekeeper");
        assertThat(returned.getId()).isNotEqualTo(peacekeeper.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(returned);
    }
}
