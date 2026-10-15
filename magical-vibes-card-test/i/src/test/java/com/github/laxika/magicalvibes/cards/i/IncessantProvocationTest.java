package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({IncessantProvocation.class, GrizzlyBears.class, Mountain.class})
class IncessantProvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, steals, and grants haste to the target creature")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castProvocation(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The stolen creature must attack while it can")
    void stolenCreatureMustAttack() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castProvocation(target);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The must-attack ability remains after temporary control and haste expire")
    void mustAttackAbilityIsPerpetual() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castProvocation(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new IncessantProvocation()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castProvocation(Permanent target) {
        harness.setHand(player1, List.of(new IncessantProvocation()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Can untap and grant haste to a creature you already control")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();

        castProvocation(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped creature is not required to attack")
    void tappedCreatureNeedNotAttack() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castProvocation(target);
        target.tap();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The perpetual attack requirement survives returning to hand and being recast")
    void attackRequirementSurvivesZoneChanges() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castProvocation(target);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.assertInHand(player2, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, gd.playerHands.get(player2.getId()).size() - 1);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isFalse();
        returned.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
