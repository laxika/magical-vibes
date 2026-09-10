package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlackPantherHopeEnduring;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingTChalla.class, BlackPantherHopeEnduring.class, GrizzlyBears.class, Shock.class})
class KingTChallaTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using either face")
    void castsUsingEitherFace() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new KingTChalla(), new KingTChalla()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Draws when any player draws their second card of the turn")
    void drawsOnAnyPlayersSecondCard() {
        Permanent king = addFrontReady(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(king.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms at sorcery speed")
    void transforms() {
        Permanent king = addFrontReady(player1);
        prepareMainPhase();
        addTransformMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(king.isTransformed()).isTrue();
        assertThat(king.getCard()).isInstanceOf(BlackPantherHopeEnduring.class);
    }

    @Test
    @DisplayName("Black Panther draws after combat damage and prevents all damage to itself")
    void backFaceAbilitiesWork() {
        Permanent panther = addBackReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        panther.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);
        harness.castInstant(player2, 0, panther.getId());
        harness.passBothPriorities();

        assertThat(panther.getMarkedDamage()).isZero();
    }

    private Permanent addFrontReady(Player player) {
        return addCreatureReady(player, new KingTChalla());
    }

    private Permanent addBackReady(Player player) {
        KingTChalla card = new KingTChalla();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void prepareMainPhase() {
        prepareMainPhase(player1);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
