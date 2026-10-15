package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DarkEndurance.class, GrizzlyBears.class})
class DarkEnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("Costs only {B} when targeting a blocking creature and grants +2/+0 and indestructible")
    void boostsBlockingCreatureAtReducedCost() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and indestructible wear off at cleanup")
    void effectsWearOff() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot pay only the reduced cost when targeting a nonblocking creature")
    void reducedCostDoesNotApplyToNonblockingCreature() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonblocking creature receives both effects when the full cost is paid")
    void boostsNonblockingCreatureAtFullCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Targeting a blocker does not reduce the black mana requirement")
    void blockingTargetStillRequiresBlackMana() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature that stops blocking before resolution still receives both effects")
    void blockingConditionIsCheckedOnlyWhenCasting() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted indestructible lets the creature survive lethal marked damage")
    void indestructiblePreventsLethalDamageDestruction() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        blocker.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private void setupSpell() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkEndurance()));
    }

    private Permanent addBlockingBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setBlocking(true);
        return bear;
    }
}
