package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormWindrider.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, DualShot.class, Shock.class})
class StormWindriderTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with flying can't attack Storm Windrider's controller")
    void flyingCreatureCannotAttackController() {
        harness.addToBattlefield(player2, new StormWindrider());
        addCreatureReady(player1, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> harness.getCombatAttackService().handleDeclareAttackersStep(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                .isNull();
    }

    @Test
    @DisplayName("Creatures without flying can attack Storm Windrider's controller")
    void nonFlyingCreatureCanAttackController() {
        harness.addToBattlefield(player2, new StormWindrider());
        addCreatureReady(player1, new GrizzlyBears());
        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("A flying creature can't block a creature controlled by Storm Windrider's controller")
    void flyingCreatureCannotBlockControllersCreature() {
        harness.addToBattlefield(player1, new StormWindrider());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block creatures you control");
    }

    @Test
    @DisplayName("A spell targeting multiple creatures gives all of them flying until end of turn")
    void targetedCreaturesGainFlying() {
        harness.addToBattlefield(player1, new StormWindrider());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(second.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A spell targeting one creature gives that creature flying")
    void singleTargetCreatureGainsFlying() {
        harness.addToBattlefield(player1, new StormWindrider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger the flying ability")
    void playerTargetDoesNotTrigger() {
        harness.addToBattlefield(player1, new StormWindrider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
