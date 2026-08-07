package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DowndraftTest extends BaseCardTest {

    @Test
    @DisplayName("{G} strips flying from a target creature until end of turn")
    void stripsFlyingUntilEndOfTurn() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing deals 2 damage to each creature with flying only")
    void sacrificeDamagesFlyersOnly() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Downdraft");
        harness.assertInGraveyard(player1, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Llanowar Elves").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature that lost flying is not damaged by the sacrifice")
    void strippedCreatureAvoidsTheDamage() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }
}
