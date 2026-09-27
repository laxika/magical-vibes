package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrsineMonstrosity.class, Forest.class, Millstone.class, Shock.class})
class UrsineMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Ursine Monstrosity mills and attacks a random opponent")
    void beginningOfCombatAbility() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setLibrary(player1, List.of(new Millstone()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(ursine.isMustAttackThisCombat()).isTrue();
        assertThat(ursine.getMustAttackTargetId()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ursine)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The beginning-of-combat ability does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setLibrary(player1, List.of(new Millstone()));

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(ursine.isMustAttackThisCombat()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and indestructible wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent ursine = addCreatureReady(player1, new UrsineMonstrosity());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setLibrary(player1, List.of(new Millstone()));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, ursine)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ursine, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
