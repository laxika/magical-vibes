package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoomskarOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when its controller casts their second spell each turn")
    void gainsLifeForSecondSpell() {
        addCreatureReady(player1, new DoomskarOracle());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

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
}
