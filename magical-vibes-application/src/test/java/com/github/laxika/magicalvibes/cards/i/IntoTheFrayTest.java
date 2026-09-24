package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.o.OneWithNothing;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
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

@CardUsed({IntoTheFray.class, ArabaMothrider.class, SpiritualVisit.class, OneWithNothing.class,
        OboroPalaceInTheClouds.class})
class IntoTheFrayTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature must attack this turn if able")
    void targetMustAttackThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("The attack requirement wears off at end of turn")
    void attackRequirementWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves the card in hand")
    void splicesOntoArcaneSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        SpiritualVisit spiritualVisit = new SpiritualVisit();
        IntoTheFray intoTheFray = new IntoTheFray();
        harness.setHand(player1, List.of(spiritualVisit, intoTheFray));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(intoTheFray);
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(new OneWithNothing(), new IntoTheFray()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }

    @Test
    @DisplayName("Requires the target to be declared as an attacker when it can attack")
    void requiresAttackWhenAble() {
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Does not require a creature unable to attack to attack")
    void doesNotRequireAttackWhenUnable() {
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }
}
