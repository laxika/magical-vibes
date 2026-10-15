package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLastRoninsTechnique.class, GrizzlyBears.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class TheLastRoninsTechniqueTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void stopBeforeCombatDamage() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
    }

    @Test
    @DisplayName("Creates three untapped, nonattacking Ninja Turtle Spirit tokens when cast normally")
    void createsTokensNormally() {
        harness.castFromHand(player1, new TheLastRoninsTechnique(), "{3}{W}");

        harness.passBothPriorities();

        List<Permanent> tokens = ninjaTurtleSpiritTokens(player1);
        assertThat(tokens).hasSize(3).allSatisfy(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and creates three tapped attacking tokens")
    void sneaksAndCreatesAttackingTokens() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new TheLastRoninsTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(ninjaTurtleSpiritTokens(player1)).hasSize(3).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("Sneak is not available outside your declare blockers step")
    void sneakIsRestrictedToYourDeclareBlockersStep() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new TheLastRoninsTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal casting during combat still creates untapped nonattacking tokens")
    void normalCastingDuringCombatDoesNotSneak() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new TheLastRoninsTechnique(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(ninjaTurtleSpiritTokens(player1)).hasSize(3).allSatisfy(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
    }

    @Test
    @DisplayName("Sneak requires returning an unblocked attacker")
    void sneakRequiresAnAttacker() {
        harness.setHand(player1, List.of(new TheLastRoninsTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({TheLastRoninsTechnique.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Sneaked tokens may attack a battle protected by the opponent")
    void sneakedTokensCanAttackABattle() {
        harness.castFromHand(player1, new TheLastRoninsTechnique(), "{3}{W}");
        harness.passBothPriorities();
        Permanent attacker = ninjaTurtleSpiritTokens(player1).getFirst();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());

        harness.setHand(player1, List.of(new TheLastRoninsTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(battle.getId());
        harness.handlePermanentChosen(player1, battle.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(ninjaTurtleSpiritTokens(player1).stream().filter(Permanent::isAttacking).toList())
                .hasSize(3)
                .anySatisfy(token -> assertThat(token.getAttackTarget()).isEqualTo(battle.getId()));
    }

    private List<Permanent> ninjaTurtleSpiritTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ninja Turtle Spirit"))
                .toList();
    }
}
