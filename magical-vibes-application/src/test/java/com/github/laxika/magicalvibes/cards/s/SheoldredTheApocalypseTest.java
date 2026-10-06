package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.e.ExtinguishTheLight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheoldredTheApocalypse.class, Swamp.class, SilverScrutiny.class, DressDown.class,
        ExtinguishTheLight.class})
class SheoldredTheApocalypseTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 2 life when drawing a card")
    void controllerDrawTriggersLifeGain() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.setLife(player1, 10);

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Opponent loses 2 life when drawing a card")
    void opponentDrawTriggersLifeLoss() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Each card in a multi-card draw creates a separate life-gain trigger")
    void controllerDrawsMultipleCards() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLife(player1, 10);

        harness.castSorceryForX(player1, 0, 3, Map.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Each card drawn by an opponent creates a separate life-loss trigger")
    void opponentDrawsMultipleCards() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setHand(player2, List.of(new SilverScrutiny()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.castSorceryForX(player2, 0, 3, Map.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(6);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Controller draws do not trigger while Sheoldred has lost all abilities")
    void controllerDrawDoesNotTriggerWithoutAbilities() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.addToBattlefield(player1, new DressDown());
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 10);

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Opponent draws do not trigger while Sheoldred has lost all abilities")
    void opponentDrawDoesNotTriggerWithoutAbilities() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.addToBattlefield(player1, new DressDown());
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 10);

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A pending life-gain trigger resolves after Sheoldred leaves the battlefield")
    void controllerTriggerSurvivesSourceRemoval() {
        var sheoldred = harness.addToBattlefieldAndReturn(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.setLife(player1, 10);

        advanceToDraw(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new ExtinguishTheLight()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, sheoldred.getId());
        harness.assertNotOnBattlefield(player1, "Sheoldred, the Apocalypse");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("A pending life-loss trigger resolves after Sheoldred leaves the battlefield")
    void opponentTriggerSurvivesSourceRemoval() {
        var sheoldred = harness.addToBattlefieldAndReturn(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.setLife(player2, 10);

        advanceToDraw(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new ExtinguishTheLight()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, sheoldred.getId());
        harness.assertNotOnBattlefield(player1, "Sheoldred, the Apocalypse");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
