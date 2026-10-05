package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ExpressiveIteration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverSpeedster.class, GrizzlyBears.class, ExpressiveIteration.class})
class QuicksilverSpeedsterTest extends BaseCardTest {

    @Test
    @DisplayName("While tapped, its controller may cast spells as though they had flash")
    void grantsFlashWhileTapped() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverSpeedster());
        quicksilver.tap();
        prepareOpponentTurnCast();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("While untapped, it does not grant flash")
    void doesNotGrantFlashWhileUntapped() {
        addCreatureReady(player1, new QuicksilverSpeedster());
        prepareOpponentTurnCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tapped Quicksilver also permits sorceries on the opponent's turn")
    void grantsFlashToSorceries() {
        addCreatureReady(player1, new QuicksilverSpeedster()).tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Untapping Quicksilver immediately ends its casting permission")
    void losesFlashPermissionWhenUntapped() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverSpeedster());
        quicksilver.tap();
        prepareOpponentTurnCast();
        quicksilver.untap();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A tapped Quicksilver outside the battlefield grants no permission")
    void losesFlashPermissionWhenSourceLeaves() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverSpeedster());
        quicksilver.tap();
        prepareOpponentTurnCast();
        gd.playerBattlefields.get(player1.getId()).remove(quicksilver);
        harness.setGraveyard(player1, List.of(quicksilver.getCard()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A tapped Quicksilver grants permission only to its controller")
    void doesNotGrantFlashToOpponent() {
        addCreatureReady(player2, new QuicksilverSpeedster()).tap();
        prepareOpponentTurnCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Quicksilver itself can be cast on the opponent's turn without a tapped source")
    void hasIntrinsicFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QuicksilverSpeedster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Quicksilver attacks on its entry turn and deals damage in both combat damage steps")
    void hasteAndDoubleStrikeApplyInCombat() {
        harness.enterBattlefieldAndReturn(player1, new QuicksilverSpeedster());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    private void prepareOpponentTurnCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.ensurePriority(player1);
    }
}
