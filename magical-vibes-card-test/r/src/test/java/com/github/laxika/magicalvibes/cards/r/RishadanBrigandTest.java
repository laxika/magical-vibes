package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishadanBrigand.class, FreshVolunteers.class, RishadanAirship.class, Island.class})
class RishadanBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent may pay {3} to keep their permanents")
    void opponentMayPayToKeepPermanent() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castRishadanBrigand();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent who declines sacrifices a permanent of their choice")
    void opponentDeclinesAndSacrificesPermanent() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        castRishadanBrigand();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("An opponent with no permanents does not need to pay")
    void opponentWithNoPermanentsDoesNotNeedToPay() {
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castRishadanBrigand();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent chooses which permanent to sacrifice")
    void opponentChoosesPermanentToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        castRishadanBrigand();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("Can block flying creatures")
    void canBlockFlyingCreatures() {
        Permanent brigand = addCreatureReady(player2, new RishadanBrigand());

        Permanent flyingAttacker = addCreatureReady(player1, new RishadanAirship());
        flyingAttacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(brigand.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block non-flying creatures")
    void cannotBlockNonFlyingCreatures() {
        addCreatureReady(player2, new RishadanBrigand());

        Permanent nonFlyingAttacker = addCreatureReady(player1, new FreshVolunteers());
        nonFlyingAttacker.setAttacking(true);

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("An opponent may pay even with no permanents to sacrifice")
    void opponentMayPayWithNoPermanents() {
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castRishadanBrigand();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may sacrifice a land and keeps unspent mana")
    void opponentSacrificesLand() {
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new FreshVolunteers());
        castRishadanBrigand();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Island");
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        harness.assertOnBattlefield(player1, "Rishadan Brigand");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Colored mana can pay the generic cost without sacrificing any permanent")
    void opponentPaysGenericCostWithColoredMana() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player2, ManaColor.BLUE, 3);
        castRishadanBrigand();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private void castRishadanBrigand() {
        harness.castFromHand(player1, new RishadanBrigand(), "{4}{U}");
    }
}
