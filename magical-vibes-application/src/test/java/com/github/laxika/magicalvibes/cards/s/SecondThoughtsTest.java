package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecondThoughtsTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void castSecondThoughts(Permanent target) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SecondThoughts()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
    }

    @Test
    @DisplayName("Exiles the target attacking creature and draws a card")
    void exilesAttackerAndDrawsCard() {
        Permanent attacker = addAttacker();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castSecondThoughts(attacker);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addAttacker();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.isAttacking())
                .findFirst()
                .orElseThrow();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SecondThoughts()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Does not draw if the target is removed before resolution")
    void fizzlesWithoutDrawingIfTargetIsRemoved() {
        Permanent attacker = addAttacker();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castSecondThoughts(attacker);
        int handSizeAfterCast = harness.getGameData().playerHands.get(player2.getId()).size();
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeAfterCast);
        harness.assertInGraveyard(player2, "Second Thoughts");
        assertThat(gd.exiledCards)
                .noneMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }
}
