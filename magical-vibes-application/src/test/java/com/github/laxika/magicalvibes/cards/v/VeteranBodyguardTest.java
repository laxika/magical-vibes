package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranBodyguard.class, GrizzlyBears.class, WhiteKnight.class, ProdigalSorcerer.class})
class VeteranBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Veteran Bodyguard absorbs damage from an unblocked creature")
    void untappedBodyguardAbsorbsUnblockedDamage() {
        Permanent bodyguard = addBodyguard();
        addAttacker();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguard.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapped Veteran Bodyguard does not absorb damage")
    void tappedBodyguardDoesNotAbsorbDamage() {
        Permanent bodyguard = addBodyguard();
        bodyguard.tap();
        addAttacker();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bodyguard.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A Bodyguard that loses all abilities does not absorb damage")
    void bodyguardWithoutAbilitiesDoesNotAbsorbDamage() {
        Permanent bodyguard = addBodyguard();
        bodyguard.setLosesAllAbilitiesUntilEndOfTurn(true);
        addAttacker();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bodyguard.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Trample damage from a blocked creature is not absorbed")
    void trampleDamageFromBlockedCreatureIsNotAbsorbed() {
        Permanent bodyguard = addBodyguard();

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(3);
        attackerCard.setKeywords(Set.of(Keyword.TRAMPLE));
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bodyguard.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A Bodyguard that dies in first-strike combat does not absorb regular combat damage")
    void bodyguardThatDiesBeforeRegularDamageDoesNotAbsorbIt() {
        Permanent bodyguard = addBodyguard();
        bodyguard.setBlocking(true);
        bodyguard.addBlockingTarget(0);

        WhiteKnight firstStrikeCard = new WhiteKnight();
        firstStrikeCard.setPower(5);
        Permanent firstStrikeAttacker = addCreatureReady(player1, firstStrikeCard);
        firstStrikeAttacker.setAttacking(true);
        addAttacker();

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bodyguard);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An untapped Bodyguard absorbs noncombat damage from an unblocked creature")
    void absorbsNoncombatDamageFromUnblockedCreature() {
        Permanent bodyguard = addBodyguard();
        ProdigalSorcerer sorcererCard = new ProdigalSorcerer();
        sorcererCard.setPower(0);
        Permanent sorcerer = addCreatureReady(player1, sorcererCard);
        sorcerer.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguard.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocked creature's noncombat damage is not absorbed")
    void blockedCreatureNoncombatDamageIsNotAbsorbed() {
        Permanent bodyguard = addBodyguard();
        ProdigalSorcerer sorcererCard = new ProdigalSorcerer();
        sorcererCard.setPower(0);
        Permanent sorcerer = addCreatureReady(player1, sorcererCard);
        sorcerer.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(sorcerer.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bodyguard.getMarkedDamage()).isZero();
    }

    private Permanent addBodyguard() {
        return addCreatureReady(player2, new VeteranBodyguard());
    }

    private void addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
    }
}
