package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelentlessXATM092.class, GrizzlyBears.class})
class RelentlessXATM092Test extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard tapped with a finality counter")
    void returnsFromGraveyardTappedWithFinalityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new RelentlessXATM092()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent relentless = findPermanent(player1, "Relentless X-ATM092");
        assertThat(relentless.isTapped()).isTrue();
        assertThat(relentless.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("A finality counter exiles it instead of putting it into a graveyard")
    void finalityCounterExilesItInsteadOfDying() {
        Permanent relentless = addRelentlessReady(player1);
        relentless.setCounterCount(CounterType.FINALITY, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, relentless));

        harness.assertNotOnBattlefield(player1, "Relentless X-ATM092");
        harness.assertNotInGraveyard(player1, "Relentless X-ATM092");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Relentless X-ATM092"));
    }

    @Test
    @DisplayName("Can't be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThree() {
        addRelentlessAttacking();
        addBlockers(3);
        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Can be blocked by three creatures")
    void canBeBlockedByThree() {
        addRelentlessAttacking();
        addBlockers(3);
        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    private Permanent addRelentlessReady(Player player) {
        Permanent relentless = new Permanent(new RelentlessXATM092());
        relentless.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(relentless);
        return relentless;
    }

    private void addRelentlessAttacking() {
        Permanent relentless = addRelentlessReady(player1);
        relentless.setAttacking(true);
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
