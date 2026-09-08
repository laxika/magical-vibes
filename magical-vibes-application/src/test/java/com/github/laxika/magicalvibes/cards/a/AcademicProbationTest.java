package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcademicProbationTest extends BaseCardTest {

    @Test
    @DisplayName("Name mode prevents opponents from casting the chosen spell")
    void nameModePreventsOpponentsFromCastingChosenSpell() {
        harness.setHand(player1, List.of(new AcademicProbation()));
        harness.setHand(player2, List.of(new Shock()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shock");

        assertThat(gd.opponentsCantCastNamedSpellsUntilControllerNextTurn.get(player1.getId()))
                .contains("Shock");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Name mode does not allow choosing a land name")
    void nameModeDoesNotAllowChoosingLandName() {
        harness.setHand(player1, List.of(new AcademicProbation()));
        harness.setHand(player2, List.of(new Forest(), new Shock()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Forest"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Shock");
    }

    @Test
    @DisplayName("Permanent mode locks attacking and activated abilities until the caster's next turn")
    void permanentModeLocksTargetUntilNextTurn() {
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AcademicProbation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.tapPermanent(player2, indexOf(player2, target)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(target)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Permanent mode prevents the target from blocking")
    void permanentModePreventsBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcademicProbation()));
        addManaForSpell();

        harness.castModalSorcery(player1, 0, 1, List.of(blocker.getId()));
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Permanent mode cannot target a land")
    void permanentModeCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AcademicProbation()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(indexOf(player2, creature)));
    }
}
