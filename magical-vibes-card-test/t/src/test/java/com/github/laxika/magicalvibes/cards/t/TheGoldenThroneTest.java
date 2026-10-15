package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MarneusCalgar;
import com.github.laxika.magicalvibes.cards.p.Poxwalkers;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheGoldenThrone.class, Poxwalkers.class, MarneusCalgar.class, SongOfTheDryads.class})
class TheGoldenThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and sets its controller's life total to 1 instead of losing")
    void replacesGameLoss() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(throne);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
    }

    @Test
    @DisplayName("Tapping and sacrificing a creature adds three mana in the chosen combination")
    void sacrificeCreatureAddsThreeManaInAnyCombination() {
        harness.addToBattlefield(player1, new TheGoldenThrone());
        Permanent sacrificedCreature = harness.addToBattlefieldAndReturn(player1, new Poxwalkers());
        harness.addToBattlefield(player1, new Poxwalkers());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificedCreature);
    }

    @Test
    void poisonStillCausesImmediateLossAfterReplacement() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        harness.assertLife(player1, 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void commanderDamageStillCausesImmediateLossAfterReplacement() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        var commander = harness.addToBattlefieldAndReturn(player2, new MarneusCalgar());
        gd.format = DeckFormat.COMMANDER;
        gd.commanderDamageReceived.put(player1.getId(), Map.of(commander.getCard().getId(), 21));

        harness.runStateBasedActions();

        harness.assertLife(player1, 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void emptyLibraryLossIsReplacedUntilAnotherDrawAttempt() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.runStateBasedActions();

        harness.assertLife(player1, 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void negativeLifeIsRaisedToOneButThroneCannotSaveAgain() {
        harness.addToBattlefield(player1, new TheGoldenThrone());
        harness.setLife(player1, -4);

        harness.runStateBasedActions();

        harness.assertLife(player1, 1);
        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(5);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void doesNotProtectOpponent() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        harness.setLife(player2, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(throne);
    }

    @Test
    @CardUsed({TheGoldenThrone.class, SongOfTheDryads.class})
    void cannotReplaceLossAfterPrintedAbilitiesAreRemoved() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, throne.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(throne);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(throne.getCard());
    }

    @Test
    void canChooseTheSameManaColorThreeTimes() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        var creature = harness.addToBattlefieldAndReturn(player1, new Poxwalkers());
        harness.addToBattlefield(player1, new Poxwalkers());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(throne.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutCreatureToSacrifice() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        harness.addToBattlefield(player2, new Poxwalkers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(throne.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
