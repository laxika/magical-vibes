package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DenyTheWitch;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightRampager.class, Murder.class, JaceBeleren.class, LeylineOfSanctity.class, DenyTheWitch.class})
class KnightRampagerTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Knight Rampager must attack the randomly chosen opponent")
    void mustAttackRandomlyChosenOpponent() {
        Permanent knight = addReadyKnight(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(knight.isMustAttackThisCombat()).isTrue();
        assertThat(knight.getMustAttackTargetId()).isEqualTo(player2.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("When Knight Rampager dies, it deals 4 damage to an opponent")
    void deathTriggerDamagesOpponent() {
        Permanent knight = addReadyKnight(player1);
        harness.setLife(player2, 20);

        killKnight(knight);

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The death trigger damages the opponent rather than choosing their planeswalker")
    void deathTriggerDoesNotTargetOpponentPlaneswalker() {
        Permanent knight = addReadyKnight(player1);
        harness.addToBattlefield(player2, new JaceBeleren());
        harness.setLife(player2, 20);

        killKnight(knight);

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The random target excludes an opponent with hexproof")
    void deathTriggerCannotTargetHexproofOpponent() {
        Permanent knight = addReadyKnight(player1);
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLife(player2, 20);

        killKnight(knight);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Frenzied Rampage does not trigger during an opponent's combat")
    void doesNotRequireAttackingDuringOpponentsCombat() {
        Permanent knight = addReadyKnight(player1);

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(knight.isMustAttackThisCombat()).isFalse();
        assertThat(knight.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("A tapped Knight Rampager is not required to attack")
    void tappedKnightMayRemainOutOfCombat() {
        Permanent knight = addReadyKnight(player1);
        knight.setTapped(true);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        declareAttackers(player1, List.of());

        assertThat(knight.isAttacking()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A summoning-sick Knight Rampager is not required to attack")
    void summoningSickKnightMayRemainOutOfCombat() {
        harness.addToBattlefield(player1, new KnightRampager());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        declareAttackers(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Frenzied Rampage can choose an opponent with hexproof")
    void combatChoiceDoesNotTargetOpponent() {
        addReadyKnight(player1);
        harness.addToBattlefield(player2, new LeylineOfSanctity());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The death trigger loses its target if the opponent gains hexproof before resolution")
    void deathTriggerRechecksTargetLegality() {
        Permanent knight = addReadyKnight(player1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, knight.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Knight Rampager controlled by the opponent damages you when it dies")
    void deathTriggerUsesDyingCreaturesController() {
        Permanent knight = addReadyKnight(player2);

        killKnight(knight);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A previous combat's chosen opponent does not constrain a later combat with a countered trigger")
    void attackTargetRequirementExpiresAfterCombat() {
        Permanent knight = addReadyKnight(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        knight.setTapped(true);
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        declareAttackers(player1, List.of());
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        knight.setTapped(false);

        advanceToBeginningOfCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new DenyTheWitch()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getCard().getId());
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, jace.getId()));
        resolveCombat();

        harness.assertInGraveyard(player2, "Jace Beleren");
    }

    private Permanent addReadyKnight(Player player) {
        return addCreatureReady(player, new KnightRampager());
    }

    private void killKnight(Permanent knight) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, knight.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
