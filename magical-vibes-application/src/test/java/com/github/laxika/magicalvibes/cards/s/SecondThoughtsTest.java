package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({SecondThoughts.class, DwarvenGrunt.class})
class SecondThoughtsTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new DwarvenGrunt());
        attacker.setAttacking(true);
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
        harness.setLibrary(player2, List.of(new DwarvenGrunt()));

        castSecondThoughts(attacker);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Dwarven Grunt");
        harness.assertNotInGraveyard(player1, "Dwarven Grunt");
        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card().getName().equals("Dwarven Grunt"));
        harness.assertInHand(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Does not draw if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttacking() {
        Permanent attacker = addAttacker();
        harness.setLibrary(player2, List.of(new DwarvenGrunt()));

        castSecondThoughts(attacker);
        int handSizeAfterCast = harness.getGameData().playerHands.get(player2.getId()).size();
        attacker.setAttacking(false);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeAfterCast);
        harness.assertOnBattlefield(player1, "Dwarven Grunt");
        harness.assertInGraveyard(player2, "Second Thoughts");
        assertThat(gd.exiledCards)
                .noneMatch(exiled -> exiled.card().getName().equals("Dwarven Grunt"));
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addAttacker();
        harness.addToBattlefield(player1, new DwarvenGrunt());
        Permanent target = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.isAttacking())
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> castSecondThoughts(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Does not draw if the target is removed before resolution")
    void fizzlesWithoutDrawingIfTargetIsRemoved() {
        Permanent attacker = addAttacker();
        harness.setLibrary(player2, List.of(new DwarvenGrunt()));

        castSecondThoughts(attacker);
        int handSizeAfterCast = harness.getGameData().playerHands.get(player2.getId()).size();
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeAfterCast);
        harness.assertInGraveyard(player2, "Second Thoughts");
        assertThat(gd.exiledCards)
                .noneMatch(exiled -> exiled.card().getName().equals("Dwarven Grunt"));
    }
}
