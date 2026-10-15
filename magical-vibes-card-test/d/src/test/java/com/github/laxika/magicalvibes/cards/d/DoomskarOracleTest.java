package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomskarOracle.class, LightningBolt.class})
class DoomskarOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when its controller casts their second spell each turn")
    void gainsLifeForSecondSpell() {
        addCreatureReady(player1, new DoomskarOracle());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts their second spell")
    void doesNotTriggerForOpponentsSecondSpell() {
        addCreatureReady(player1, new DoomskarOracle());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        DoomskarOracle oracle = new DoomskarOracle();
        harness.setHand(player1, List.of(oracle));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(oracle.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, oracle.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doomskar Oracle");
    }

    @Test
    @DisplayName("Creature spells count, but an Oracle does not trigger for its own casting")
    void creatureSpellsCountWithoutSelfTriggering() {
        harness.setHand(player1, List.of(new DoomskarOracle(), new DoomskarOracle()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        resolveAllTriggers();
        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting Oracle as the second spell does not trigger its own ability")
    void enteringAfterSecondSpellDoesNotTriggerRetroactively() {
        harness.setHand(player1, List.of(new LightningBolt(), new DoomskarOracle(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers on the controller's second spell during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        addCreatureReady(player1, new DoomskarOracle());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The second-spell count resets when the next turn begins")
    void secondSpellCountResetsEachTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        addCreatureReady(player1, new DoomskarOracle());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player2, List.of(new DoomskarOracle(), new DoomskarOracle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 22);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 22);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        resolveAllTriggers();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Foretelling is not a spell and does not advance the second-spell count")
    void foretellingDoesNotCountAsCasting() {
        addCreatureReady(player1, new DoomskarOracle());
        harness.setHand(player1, List.of(new DoomskarOracle(), new DoomskarOracle(), new DoomskarOracle()));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.setLife(player1, 20);

        harness.foretell(player1, 0);
        harness.assertLife(player1, 20);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        resolveAllTriggers();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A foretold Oracle cannot be cast on the turn it was foretold")
    void cannotCastOnForetellTurn() {
        DoomskarOracle oracle = new DoomskarOracle();
        harness.setHand(player1, List.of(oracle));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, oracle.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(oracle.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Doomskar Oracle");
    }

    @Test
    @DisplayName("Foretell cannot be used during an opponent's turn")
    void cannotForetellDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new DoomskarOracle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting a foretold Oracle counts as a spell for an Oracle already on the battlefield")
    void foretoldCastCountsAsSecondSpell() {
        addCreatureReady(player1, new DoomskarOracle());
        DoomskarOracle foretold = new DoomskarOracle();
        harness.setHand(player1, List.of(foretold));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.setHand(player1, List.of(new DoomskarOracle()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 20);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.castFromExile(player1, foretold.getId());
        harness.passBothPriorities();

        resolveAllTriggers();
        harness.assertLife(player1, 24);
        assertThat(gd.findExiledCard(foretold.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }
}
