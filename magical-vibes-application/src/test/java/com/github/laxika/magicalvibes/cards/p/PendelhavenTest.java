package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pendelhaven.class, TundraWolves.class, DurkwoodBoars.class, ZephyrFalcon.class})
class PendelhavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pendelhaven adds one green mana")
    void tapsForGreenMana() {
        Permanent pendelhaven = addPendelhavenReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pendelhaven.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability gives a 1/1 creature +1/+2 until end of turn")
    void boostsOneOneCreature() {
        addPendelhavenReady(player1);
        Permanent wolves = addCreatureReady(player1, new TundraWolves());

        harness.activateAbility(player1, 0, 1, null, wolves.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addPendelhavenReady(player1);
        Permanent wolves = addCreatureReady(player1, new TundraWolves());

        harness.activateAbility(player1, 0, 1, null, wolves.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a creature that is not 1/1")
    void cannotTargetNonOneOneCreature() {
        addPendelhavenReady(player1);
        Permanent boars = addCreatureReady(player1, new DurkwoodBoars());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, boars.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Ability can target an opponent's 1/1 creature")
    void canTargetOpponentCreature() {
        addPendelhavenReady(player1);
        Permanent falcon = addCreatureReady(player2, new ZephyrFalcon());

        harness.activateAbility(player1, 0, 1, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, falcon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability rechecks that the target is 1/1 when it resolves")
    void targetMustStillBeOneOneWhenAbilityResolves() {
        Permanent firstPendelhaven = addPendelhavenReady(player1);
        Permanent secondPendelhaven = addPendelhavenReady(player2);
        Permanent wolves = addCreatureReady(player1, new TundraWolves());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(firstPendelhaven),
                1, null, wolves.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(secondPendelhaven),
                1, null, wolves.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolves)).isEqualTo(3);
    }

    private Permanent addPendelhavenReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Pendelhaven());
    }
}
