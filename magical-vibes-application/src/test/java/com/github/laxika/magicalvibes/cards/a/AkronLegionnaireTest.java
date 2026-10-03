package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkronLegionnaire.class, BarbaryApes.class, BronzeHorse.class})
class AkronLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("A non-artifact creature you control cannot attack while Akron Legionnaire is out")
    void nonArtifactCreatureCannotAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent apes = addCreatureReady(player1, new BarbaryApes());

        assertThatThrownBy(() -> declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(apes))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Akron Legionnaire itself can attack")
    void akronCanAttack() {
        Permanent akron = addCreatureReady(player1, new AkronLegionnaire());

        harness.setLife(player2, 20);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(akron)));

        // Akron is 8/4, unblocked
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Another creature named Akron Legionnaire can attack")
    void anotherAkronLegionnaireCanAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());
        Permanent secondAkron = addCreatureReady(player1, new AkronLegionnaire());

        harness.setLife(player2, 20);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(secondAkron)));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("An artifact creature you control can still attack")
    void artifactCreatureCanAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent horse = addCreatureReady(player1, new BronzeHorse());

        assertThatCode(() -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(horse))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Opponent's non-artifact creatures are unaffected (restriction is controller-scoped)")
    void opponentCreatureUnaffected() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent apes = addCreatureReady(player2, new BarbaryApes());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(apes)));

        assertThat(apes.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Non-artifact creatures can attack after the last Akron Legionnaire dies")
    void restrictionEndsWhenAkronDies() {
        Permanent akron = harness.addToBattlefieldAndReturn(player1, new AkronLegionnaire());
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        akron.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Akron Legionnaire");

        harness.setLife(player2, 20);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(apes)));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("One remaining Akron Legionnaire still prevents non-artifact creatures from attacking")
    void restrictionRemainsWhileAnotherAkronSurvives() {
        Permanent akron = harness.addToBattlefieldAndReturn(player1, new AkronLegionnaire());
        harness.addToBattlefield(player1, new AkronLegionnaire());
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        akron.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Akron Legionnaire");

        assertThatThrownBy(() -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(apes))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-artifact creatures can block while their controller controls Akron Legionnaire")
    void nonArtifactCreatureCanBlock() {
        harness.addToBattlefield(player2, new AkronLegionnaire());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        addCreatureReady(player1, new BarbaryApes());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .doesNotThrowAnyException();
    }
}
