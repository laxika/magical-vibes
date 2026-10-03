package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlegraceAngel.class, GrizzlyBears.class})
class BattlegraceAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Ally attacking alone gets +1/+1 and lifelink")
    void allyAttackingAloneBoostedAndLifelink() {
        addCreatureReady(player1, new BattlegraceAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The Angel attacking alone boosts itself and gains lifelink")
    void selfAttackingAloneBoostedAndLifelink() {
        Permanent angel = addCreatureReady(player1, new BattlegraceAngel());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
        assertThat(angel.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lifelink and boost wear off at end of turn")
    void wearsOff() {
        addCreatureReady(player1, new BattlegraceAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new BattlegraceAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Battlegrace Angel"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Exalted and lifelink trigger separately when the Angel attacks alone")
    void separateAttackTriggers() {
        addCreatureReady(player1, new BattlegraceAngel());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Multiple Angels stack exalted but do not multiply lifelink life gain")
    void multipleAngelsGrantOneLifeGainForDamage() {
        Permanent attacker = addCreatureReady(player1, new BattlegraceAngel());
        addCreatureReady(player1, new BattlegraceAngel());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isTrue();
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("An opponent attacking alone does not trigger your Angel")
    void opponentAttackingAloneDoesNotTrigger() {
        addCreatureReady(player1, new BattlegraceAngel());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
