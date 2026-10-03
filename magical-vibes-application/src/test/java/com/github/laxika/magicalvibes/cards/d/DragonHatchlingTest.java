package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
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

@CardUsed({DragonHatchling.class})
class DragonHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +1/+0 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump ability can be activated repeatedly in a turn")
    void pumpAbilityStacks() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump can be activated while tapped and summoning sick")
    void canPumpWhileTappedAndSummoningSick() {
        Permanent hatchling = harness.addToBattlefieldAndReturn(player1, new DragonHatchling());
        hatchling.setSummoningSick(true);
        hatchling.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hatchling)).isEqualTo(1);
        assertThat(hatchling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pump uses the stack and only boosts its source")
    void pumpWaitsForResolutionAndOnlyBoostsSource() {
        Permanent hatchling = addReadyHatchling(player1);
        Permanent other = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump cannot be paid for with colorless mana")
    void cannotPumpWithColorlessMana() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, hatchling)).isEqualTo(0);
    }

    @Test
    @CardUsed({DragonHatchling.class, Unsummon.class})
    @DisplayName("Removing the source in response does not boost another hatchling")
    void removedSourceDoesNotBoostAnotherHatchling() {
        Permanent hatchling = addReadyHatchling(player1);
        Permanent other = addReadyHatchling(player1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hatchling);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @CardUsed({DragonHatchling.class, WalkingCorpse.class})
    @DisplayName("A ground creature cannot block Dragon Hatchling")
    void groundCreatureCannotBlock() {
        addReadyHatchling(player1);
        harness.addToBattlefield(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flying creature can block Dragon Hatchling")
    void flyingCreatureCanBlock() {
        addReadyHatchling(player1);
        Permanent blocker = addReadyHatchling(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @CardUsed({DragonHatchling.class, DeadlyRecluse.class})
    @DisplayName("A creature with reach can block Dragon Hatchling")
    void reachCreatureCanBlock() {
        addReadyHatchling(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DragonHatchling());
        perm.setSummoningSick(false);
        return perm;
    }
}
