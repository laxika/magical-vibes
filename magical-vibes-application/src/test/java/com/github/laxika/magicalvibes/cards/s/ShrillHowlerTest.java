package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShrillHowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        howler.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("{5}{G} transforms into Howling Chorus")
    void transformAbilityFlipsToHowlingChorus() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        assertThat(howler.isTransformed()).isTrue();
        assertThat(howler.getCard().getName()).isEqualTo("Howling Chorus");
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Howling Chorus creates an Eldrazi Horror token on combat damage to a player")
    void backFaceCreatesTokenOnCombatDamage() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        howler.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities(); // resolve create-token trigger

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Eldrazi Horror"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELDRAZI, CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Howling Chorus also can't be blocked by lower-power creatures")
    void backFaceCannotBeBlockedByLowerPower() {
        Permanent howler = addCreatureReady(player1, new ShrillHowler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        howler.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howler);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }
}
