package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExquisiteArchangel.class})
class ExquisiteArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and resets its controller's life total instead of losing")
    void exilesItselfAndResetsLifeTotal() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(angel.getCard());
    }

    @Test
    @DisplayName("Replacing a poison loss does not remove poison or prevent the next state-based loss")
    void stillLosesToPoisonAfterReplacement() {
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Exquisite Archangel"));
    }

    @Test
    @DisplayName("Only replaces one loss")
    void onlyReplacesOneLoss() {
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Simultaneous lethal damage to the Archangel and its controller still replaces the loss")
    void replacesLossDespiteSimultaneousLethalDamage() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        angel.setMarkedDamage(5);
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
    }

    @Test
    @DisplayName("An Archangel with no abilities cannot replace its controller's loss")
    void doesNotReplaceLossAfterLosingAbilities() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        angel.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(angel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(angel.getCard());
    }

    @Test
    @DisplayName("Uses the starting life total of the current format")
    void resetsToCommanderStartingLife() {
        gd.format = DeckFormat.COMMANDER;
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.setLife(player1, -4);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Replacing an empty-library loss protects until another failed draw")
    void survivesFailedDrawUntilNextFailedDraw() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        harness.setLibrary(player1, java.util.List.of());
        harness.setLife(player1, 40);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(angel.getCard());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("An opponent's Archangel cannot replace your loss")
    void doesNotProtectOpponent() {
        var angel = harness.addToBattlefieldAndReturn(player2, new ExquisiteArchangel());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel);
    }

    @Test
    @DisplayName("The controller chooses which Archangel replaces a loss")
    void choosesBetweenTwoArchangels() {
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A face-down Archangel has no loss replacement ability")
    void faceDownArchangelDoesNotReplaceLoss() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ExquisiteArchangel());
        angel.setFaceDown(2, 2, java.util.Set.of(com.github.laxika.magicalvibes.model.CardType.CREATURE));
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(angel);
    }

    @Test
    @DisplayName("Commander damage remains lethal after the life reset")
    void stillLosesToCommanderDamageAfterReplacement() {
        gd.format = DeckFormat.COMMANDER;
        harness.addToBattlefield(player1, new ExquisiteArchangel());
        gd.commanderDamageReceived.put(player1.getId(), java.util.Map.of(java.util.UUID.randomUUID(), 21));

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.getLife(player1.getId())).isEqualTo(40);
    }
}
