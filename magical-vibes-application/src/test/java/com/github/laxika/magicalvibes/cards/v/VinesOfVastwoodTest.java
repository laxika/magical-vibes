package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({VinesOfVastwood.class, GiantGrowth.class, GrizzlyBears.class, ProdigalPyromancer.class})
class VinesOfVastwoodTest extends BaseCardTest {

    @Test
    @DisplayName("The target cannot be targeted by an opponent, but its controller can target it")
    void protectsAgainstOpponentsOnly() {
        Permanent target = addCreature(player1);
        castVines(target, false);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Kicker gives the target +4/+4")
    void kickedBoostsTarget() {
        Permanent target = addCreature(player1);
        castVines(target, true);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The targeting restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        castVines(target, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Unkicked Vines does not boost the target")
    void unkickedDoesNotBoostTarget() {
        Permanent target = addCreature(player1);
        castVines(target, false);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Kicked Vines boost expires at end of turn")
    void kickedBoostExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        castVines(target, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Vines prevents the caster's opponent from targeting their own creature")
    void opponentCannotTargetTheirOwnCreature() {
        Permanent target = addCreature(player2);
        castVines(target, false);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Vines caster can still target an opponent's creature")
    void casterCanTargetOpponentsCreature() {
        Permanent target = addCreature(player2);
        castVines(target, false);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Vines makes an opponent's spell already on the stack lose its target")
    void protectsFromSpellAlreadyOnStack() {
        Permanent target = addCreature(player1);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, target.getId());

        castVines(target, false);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GiantGrowth);
    }

    @Test
    @DisplayName("Vines prevents opposing activated abilities from targeting the creature")
    void protectsAgainstActivatedAbilities() {
        Permanent target = addCreature(player1);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        castVines(target, false);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vines makes an opposing activated ability already on the stack lose its target")
    void protectsFromActivatedAbilityAlreadyOnStack() {
        Permanent target = addCreature(player1);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.activateAbility(player2, 0, null, target.getId());

        castVines(target, false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    private void castVines(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new VinesOfVastwood()));
        harness.addMana(player1, ManaColor.GREEN, kicked ? 2 : 1);
        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
