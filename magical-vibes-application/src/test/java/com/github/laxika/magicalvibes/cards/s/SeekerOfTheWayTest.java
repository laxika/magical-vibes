package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeekerOfTheWay.class, Shock.class, GrizzlyBears.class})
class SeekerOfTheWayTest extends BaseCardTest {

    private Permanent addSeeker() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SeekerOfTheWay());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return seeker;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 and lifelink until end of turn")
    void noncreatureSpellPumpsAndGrantsLifelink() {
        Permanent seeker = addSeeker();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger either ability")
    void creatureSpellDoesNotTrigger() {
        Permanent seeker = addSeeker();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The prowess boost and temporary lifelink wear off at end of turn")
    void temporaryAbilitiesWearOffAtEndOfTurn() {
        Permanent seeker = addSeeker();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isTrue();

        endTurn();

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two noncreature casts stack prowess but do not multiply lifelink life gain")
    void repeatedCastsGiveOneBoostEachAndOneLifelinkLifeGain() {
        Permanent seeker = addSeeker();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int cast = 0; cast < 2; cast++) {
            harness.castInstant(player1, 0, player2.getId());
            for (int resolution = 0; resolution < 10 && !gd.stack.isEmpty(); resolution++) {
                harness.passBothPriorities();
            }
            assertThat(gd.stack).isEmpty();
        }

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(4);
        seeker.setAttacking(true);
        seeker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger either ability")
    void opponentNoncreatureSpellDoesNotTrigger() {
        Permanent seeker = addSeeker();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isFalse();
        harness.assertLife(player1, 18);
    }
}
