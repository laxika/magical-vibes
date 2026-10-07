package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SqueakingPieSneak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarPitcher.class, SqueakingPieSneak.class, GrizzlyBears.class, BoggartShenanigans.class})
class TarPitcherTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with multiple Goblins asks to choose a sacrifice")
    void activatingWithMultipleGoblinsAsksForChoice() {
        addCreatureReady(player1, new TarPitcher());
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing another Goblin deals 2 damage to target player; Tar Pitcher survives")
    void sacrificingOtherGoblinDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TarPitcher());
        harness.addToBattlefield(player1, new SqueakingPieSneak());
        UUID sneakId = harness.getPermanentId(player1, "Squeaking Pie Sneak");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sneakId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Tar Pitcher");
        harness.assertNotOnBattlefield(player1, "Squeaking Pie Sneak");
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 2/2")
    void dealsDamageToCreatureDestroying() {
        addCreatureReady(player1, new TarPitcher());
        harness.addToBattlefield(player1, new SqueakingPieSneak());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Squeaking Pie Sneak"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can sacrifice Tar Pitcher itself and still deal damage")
    void canSacrificeItself() {
        harness.setLife(player2, 20);
        Permanent pitcher = addCreatureReady(player1, new TarPitcher());
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, pitcher.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Tar Pitcher");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new TarPitcher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent pitcher = addCreatureReady(player1, new TarPitcher());
        pitcher.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can sacrifice a noncreature Goblin permanent")
    void canSacrificeKindredGoblinEnchantment() {
        harness.setLife(player2, 20);
        Permanent pitcher = addCreatureReady(player1, new TarPitcher());
        harness.addToBattlefield(player1, new SqueakingPieSneak());
        harness.addToBattlefield(player1, new BoggartShenanigans());
        UUID shenanigansId = harness.getPermanentId(player1, "Boggart Shenanigans");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, shenanigansId);

        assertThat(pitcher.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Tar Pitcher");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The only Goblin can be sacrificed without a choice prompt")
    void canSacrificeItselfAsOnlyGoblin() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TarPitcher());

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Tar Pitcher");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
