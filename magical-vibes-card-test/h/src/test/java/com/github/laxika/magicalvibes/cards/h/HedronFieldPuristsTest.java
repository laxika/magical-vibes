package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({HedronFieldPurists.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class HedronFieldPuristsTest extends BaseCardTest {

    @Test
    @DisplayName("Level 1 prevents one damage from each source to its controller and creatures they control")
    void levelOnePreventsOneDamageToControllerAndCreature() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        levelUp(player1, purists);

        castShock(player2, player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Level 5 prevents two damage from each source")
    void levelFivePreventsTwoDamage() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        for (int i = 0; i < 5; i++) {
            levelUp(player1, purists);
        }

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(purists.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Hedron-Field Purists does not prevent damage to an opponent's creature")
    void doesNotPreventDamageToOpponentsCreature() {
        addCreatureReady(player1, new HedronFieldPurists());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        levelUp(player1, findPermanent(player1, "Hedron-Field Purists"));

        castShock(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    void levelCountersChangeBasePowerAndToughnessAtEachBoundary() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        for (int level = 1; level <= 6; level++) {
            levelUp(player1, purists);
            assertThat(gqs.getEffectivePower(gd, purists)).isEqualTo(level < 5 ? 1 : 2);
            assertThat(gqs.getEffectiveToughness(gd, purists)).isEqualTo(level < 5 ? 4 : 5);
        }
    }

    @Test
    void noPreventionBeforeFirstLevel() {
        addCreatureReady(player1, new HedronFieldPurists());
        castShock(player2, player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void levelFourStillPreventsOnlyOnePerDamageEvent() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        for (int i = 0; i < 4; i++) {
            levelUp(player1, purists);
        }
        castShock(player2, player1.getId());
        castShock(player2, player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void levelFiveProtectsItselfAndFullyPreventsShock() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        for (int i = 0; i < 5; i++) {
            levelUp(player1, purists);
        }
        castShock(player2, purists.getId());
        castShock(player2, player1.getId());
        assertThat(purists.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventionFromTwoPuristsIsCumulative() {
        Permanent first = addCreatureReady(player1, new HedronFieldPurists());
        Permanent second = addCreatureReady(player1, new HedronFieldPurists());
        levelUp(player1, first);
        levelUp(player1, second);
        castShock(player2, player1.getId());
        castShock(player2, first.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(first.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotProtectOpposingPlayer() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        levelUp(player1, purists);
        castShock(player1, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void preventsCombatDamageToController() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        levelUp(player1, purists);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void cannotLevelUpDuringCombat() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(purists.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void levelUp(Player player, Permanent purists) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(purists);
        harness.activateAbility(player, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }

    private void castShock(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
