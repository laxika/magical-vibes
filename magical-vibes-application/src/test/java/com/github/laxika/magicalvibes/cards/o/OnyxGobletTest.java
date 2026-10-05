package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnyxGoblet.class})
class OnyxGobletTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack and taps the goblet")
    void activatingTargetingPlayerPutsOnStack() {
        Permanent goblet = addReadyGoblet(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
        assertThat(goblet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target player loses 1 life on resolution")
    void targetPlayerLosesOneLife() {
        addReadyGoblet(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        addReadyGoblet(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A newly controlled noncreature goblet can activate without mana")
    void newlyControlledGobletCanActivate() {
        harness.addToBattlefield(player1, new OnyxGoblet());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A tapped goblet cannot activate")
    void tappedGobletCannotActivate() {
        Permanent goblet = addReadyGoblet(player1);
        goblet.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent goblet = addReadyGoblet(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblet.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(goblet.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability resolves after the goblet leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent goblet = addReadyGoblet(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(goblet);
        gd.playerGraveyards.get(player1.getId()).add(goblet.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyGoblet(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new OnyxGoblet());
        perm.setSummoningSick(false);
        return perm;
    }
}
