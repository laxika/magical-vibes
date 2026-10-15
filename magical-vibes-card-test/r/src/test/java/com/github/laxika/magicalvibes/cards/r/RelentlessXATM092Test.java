package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
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

@CardUsed({RelentlessXATM092.class, DoublingSeason.class})
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
        prepareDeclareBlockers();

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
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    private Permanent addRelentlessReady(Player player) {
        return addCreatureReady(player, new RelentlessXATM092());
    }

    private void addRelentlessAttacking() {
        Permanent relentless = addRelentlessReady(player1);
        relentless.setAttacking(true);
    }

    private void addBlockers(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new RelentlessXATM092());
        }
    }

    @Test
    void returnsOnlyTheActivatingCopy() {
        RelentlessXATM092 first = new RelentlessXATM092();
        RelentlessXATM092 second = new RelentlessXATM092();
        RelentlessXATM092 opposing = new RelentlessXATM092();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposing));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
        assertThat(findPermanent(player1, "Relentless X-ATM092").getCard().getId()).isEqualTo(first.getId());
    }

    @Test
    void cannotActivateWithOnlySevenMana() {
        harness.setGraveyard(player1, List.of(new RelentlessXATM092()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Relentless X-ATM092");
    }

    @Test
    void canReturnDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
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
    void multipleActivationsDoNotAddExtraFinalityCounters() {
        harness.setGraveyard(player1, List.of(new RelentlessXATM092()));
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Relentless X-ATM092")).isEqualTo(1);
        assertThat(findPermanent(player1, "Relentless X-ATM092").getCounterCount(CounterType.FINALITY))
                .isEqualTo(1);
    }

    @Test
    @CardUsed({RelentlessXATM092.class, DoublingSeason.class})
    void doublingSeasonDoublesTheFinalityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setGraveyard(player1, List.of(new RelentlessXATM092()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Relentless X-ATM092").getCounterCount(CounterType.FINALITY))
                .isEqualTo(2);
    }
}
