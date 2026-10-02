package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KherKeep;
import com.github.laxika.magicalvibes.cards.l.LaboratoryManiac;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.cards.u.UrborgSyphonMage;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelsGrace.class, UrborgSyphonMage.class, SuddenShock.class, KherKeep.class,
        LaboratoryManiac.class})
class AngelsGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Damage reduces the controller's life to 1 without ending the game")
    void damageReducesLifeToOne() {
        harness.setLife(player1, 2);
        castAngelsGrace();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Damage still lowers life after life loss leaves the controller at 0 or less")
    void damageStillLowersLifeAfterLifeLoss(int startingLife) {
        harness.setLife(player1, startingLife);
        castAngelsGrace();

        addCreatureReady(player2, new UrborgSyphonMage());
        harness.setHand(player2, List.of(new UrborgSyphonMage()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 4);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Split second blocks spells and non-mana activated abilities")
    void splitSecondBlocksResponses() {
        harness.addToBattlefield(player2, new KherKeep());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new AngelsGrace()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Angel's Grace expires during cleanup")
    void expiresAtCleanup() {
        harness.setLife(player1, 1);
        castAngelsGrace();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Prevents an opponent from winning the game during the turn")
    void opponentCannotWinTheGame() {
        harness.addToBattlefield(player2, new LaboratoryManiac());
        harness.setLibrary(player2, List.of());
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new AngelsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.DRAW);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gameLogContains("can't lose the game")).isTrue();
    }

    @Test
    @DisplayName("Split second still allows mana abilities")
    void splitSecondAllowsManaAbilities() {
        harness.addToBattlefield(player2, new KherKeep());
        harness.setHand(player1, List.of(new AngelsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Combat damage cannot lower the controller below 1 life")
    void combatDamageReducesLifeToOne() {
        harness.setLife(player1, 1);
        castAngelsGrace();
        addCreatureReady(player2, new UrborgSyphonMage());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The opponent can still lose to lethal damage")
    void opponentCanStillLose() {
        harness.setLife(player2, 1);
        castAngelsGrace();
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The controller cannot lose from drawing from an empty library")
    void emptyLibraryDoesNotCauseLoss() {
        gd.turnNumber = 2;
        harness.setLibrary(player1, List.of());
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new AngelsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.passUntil(player1, TurnStep.DRAW);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castAngelsGrace() {
        harness.setHand(player1, List.of(new AngelsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0);
    }
}
