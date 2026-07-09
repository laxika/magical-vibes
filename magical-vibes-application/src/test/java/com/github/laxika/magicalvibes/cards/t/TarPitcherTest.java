package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SqueakingPieSneak;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarPitcherTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with multiple Goblins asks to choose a sacrifice")
    void activatingWithMultipleGoblinsAsksForChoice() {
        addReadyTarPitcher(player1);
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing another Goblin deals 2 damage to target player; Tar Pitcher survives")
    void sacrificingOtherGoblinDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyTarPitcher(player1);
        harness.addToBattlefield(player1, new SqueakingPieSneak());
        UUID sneakId = harness.getPermanentId(player1, "Squeaking Pie Sneak");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sneakId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tar Pitcher"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Squeaking Pie Sneak"));
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 2/2")
    void dealsDamageToCreatureDestroying() {
        addReadyTarPitcher(player1);
        harness.addToBattlefield(player1, new SqueakingPieSneak());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Squeaking Pie Sneak"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can sacrifice Tar Pitcher itself and still deal damage")
    void canSacrificeItself() {
        harness.setLife(player2, 20);
        Permanent pitcher = addReadyTarPitcher(player1);
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, pitcher.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tar Pitcher"));
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent pitcher = new Permanent(new TarPitcher());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(pitcher);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent pitcher = addReadyTarPitcher(player1);
        pitcher.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyTarPitcher(Player player) {
        Permanent perm = new Permanent(new TarPitcher());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
