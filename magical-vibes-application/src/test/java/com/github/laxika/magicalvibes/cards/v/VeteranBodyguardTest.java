package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({VeteranBodyguard.class, GrizzlyBears.class})
class VeteranBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Veteran Bodyguard absorbs damage from an unblocked creature")
    void untappedBodyguardAbsorbsUnblockedDamage() {
        Permanent bodyguard = addBodyguard();
        addAttacker();

        advanceToCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguard.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapped Veteran Bodyguard does not absorb damage")
    void tappedBodyguardDoesNotAbsorbDamage() {
        Permanent bodyguard = addBodyguard();
        bodyguard.tap();
        addAttacker();

        advanceToCombatDamage();

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
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bodyguard.getMarkedDamage()).isZero();
    }

    private Permanent addBodyguard() {
        Permanent bodyguard = new Permanent(new VeteranBodyguard());
        bodyguard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bodyguard);
        return bodyguard;
    }

    private void addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
    }

    private void advanceToCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
