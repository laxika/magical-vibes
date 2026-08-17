package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShefetArchfiendTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives all other creatures -2/-2 but not Shefet Archfiend")
    void etbDebuffsOtherCreaturesButNotItself() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        castShefetArchfiend();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ownGiant = findPermanent(player1, "Hill Giant");
        Permanent opponentGiant = findPermanent(player2, "Hill Giant");
        Permanent archfiend = findPermanent(player1, "Shefet Archfiend");

        assertThat(ownGiant.getEffectivePower()).isEqualTo(1);
        assertThat(ownGiant.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentGiant.getEffectivePower()).isEqualTo(1);
        assertThat(opponentGiant.getEffectiveToughness()).isEqualTo(1);
        assertThat(archfiend.getEffectivePower()).isEqualTo(5);
        assertThat(archfiend.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB debuff wears off at cleanup")
    void etbDebuffWearsOffAtCleanup() {
        harness.addToBattlefield(player2, new HillGiant());

        castShefetArchfiend();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);

        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cycling {2} discards Shefet Archfiend and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ShefetArchfiend()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shefet Archfiend");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castShefetArchfiend() {
        harness.setHand(player1, List.of(new ShefetArchfiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }
}
