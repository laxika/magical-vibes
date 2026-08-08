package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LyevDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Detains both target creatures so neither can attack")
    void detainsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()));

        assertThatThrownBy(() -> declareAttack(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May detain a single creature (up to two)")
    void detainsSingleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(bears.getId()));

        assertThatThrownBy(() -> declareAttack(List.of(bears)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detain wears off at the caster's next turn")
    void detainWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(bears.getId()));

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(List.of(bears))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LyevDecree()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownBears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new LyevDecree()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void declareAttack(List<Permanent> creatures) {
        creatures.forEach(creature -> creature.setSummoningSick(false));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        List<Integer> indices = creatures.stream()
                .map(creature -> gd.playerBattlefields.get(player2.getId()).indexOf(creature))
                .toList();
        gs.declareAttackers(gd, player2, indices);
    }
}
