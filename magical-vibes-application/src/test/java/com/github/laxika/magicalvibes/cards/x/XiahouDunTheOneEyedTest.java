package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XiahouDunTheOneEyed.class, ForestBear.class, Coercion.class, ShuCavalry.class})
class XiahouDunTheOneEyedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns the target black card from the graveyard to hand")
    void returnsBlackCardFromGraveyard() {
        setupXiahouOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coercion");
        harness.assertNotInGraveyard(player1, "Coercion");
        // Xiahou Dun was sacrificed as a cost.
        harness.assertNotOnBattlefield(player1, "Xiahou Dun, the One-Eyed");
        harness.assertInGraveyard(player1, "Xiahou Dun, the One-Eyed");
    }

    @Test
    @DisplayName("Cannot target a non-black card in the graveyard")
    void cannotTargetNonBlackCard() {
        setupXiahouOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card green = new ForestBear();
        harness.setGraveyard(player1, List.of(green));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, green.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black card in an opponent's graveyard")
    void cannotTargetBlackCardInOpponentsGraveyard() {
        setupXiahouOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Card black = new Coercion();
        harness.setGraveyard(player2, List.of(black));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupXiahouOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupXiahouOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new XiahouDunTheOneEyed());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card black = new Coercion();
        harness.setGraveyard(player1, List.of(black));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, black.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Horsemanship prevents a creature without horsemanship from blocking")
    void horsemanshipPreventsNonHorsemanshipBlock() {
        Permanent blocker = addCreatureReady(player2, new ForestBear());
        addCreatureReady(player1, new XiahouDunTheOneEyed());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Horsemanship allows a creature with horsemanship to block")
    void horsemanshipAllowsHorsemanshipBlock() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        addCreatureReady(player1, new XiahouDunTheOneEyed());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void setupXiahouOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new XiahouDunTheOneEyed());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
