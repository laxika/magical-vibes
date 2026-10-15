package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistOfTheLonelyVigil.class, GrizzlyBears.class, Plains.class, Shock.class, Millstone.class, FieldCreeper.class})
class GeistOfTheLonelyVigilTest extends BaseCardTest {

    private Permanent readyGeist() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new GeistOfTheLonelyVigil());
        geist.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        return geist;
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Geist cannot attack with fewer than four card types in its controller's graveyard")
    void cannotAttackWithoutDelirium() {
        Permanent geist = readyGeist();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Geist can attack with four card types in its controller's graveyard")
    void canAttackWithDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        Permanent geist = readyGeist();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(index)));

        assertThat(geist.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable Geist's delirium")
    void opponentGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        Permanent geist = readyGeist();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Four graveyard cards with only three distinct types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Plains(), new Shock()));
        Permanent geist = readyGeist();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Losing delirium before attacker declaration prevents attacking")
    void losingDeliriumBeforeDeclarationPreventsAttacking() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        Permanent geist = readyGeist();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock()));
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Delirium does not allow a summoning-sick Geist to attack")
    void deliriumDoesNotBypassSummoningSickness() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        Permanent geist = readyGeist();
        geist.setSummoningSick(true);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An artifact creature supplies two card types toward delirium")
    void multipleTypesOnOneCardEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new FieldCreeper(), new Plains(), new Shock()));
        Permanent geist = readyGeist();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        beginDeclareAttackers();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(index)));

        assertThat(geist.isAttacking()).isTrue();
    }
}
