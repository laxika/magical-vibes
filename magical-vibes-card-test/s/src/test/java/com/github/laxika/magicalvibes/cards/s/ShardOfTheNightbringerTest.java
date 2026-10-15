package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShardOfTheNightbringer.class, GatherSpecimens.class})
class ShardOfTheNightbringerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, target opponent loses half their life rounded up and the controller gains it")
    void castEtbDrainsHalfLifeRoundedUp() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 21);
        castShard(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ShardOfTheNightbringer()));
        addManaForCast();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("The creature spell needs no target; its opponent target is chosen after entry")
    void choosesTargetOnlyAfterEntering() {
        harness.setHand(player1, List.of(new ShardOfTheNightbringer()));
        addManaForCast();
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shard of the Nightbringer");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Entering without being cast does not trigger Drain Life or request a target")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.enterBattlefieldAndReturn(player1, new ShardOfTheNightbringer());

        harness.assertOnBattlefield(player1, "Shard of the Nightbringer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Drain Life uses the opponent's life at resolution, including an even total")
    void usesLifeTotalAtResolution() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 21);
        castShard(player2.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 18);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Drain Life still resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        castShard(player2.getId());
        harness.passBothPriorities();
        var shard = gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.setGraveyard(player1, List.of(shard.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Drain Life does not trigger if Gather Specimens gives the creature to a player who did not cast it")
    void entryControllerMustHaveCastTheCreature() {
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0);

        castShard(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shard of the Nightbringer");
        harness.assertNotOnBattlefield(player1, "Shard of the Nightbringer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castShard(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShardOfTheNightbringer()));
        addManaForCast();
        harness.castCreature(player1, 0, targetId);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
