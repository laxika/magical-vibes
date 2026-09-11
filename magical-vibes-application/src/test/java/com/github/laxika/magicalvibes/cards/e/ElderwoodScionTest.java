package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElderwoodScion.class, GrizzlyBears.class, Murder.class})
class ElderwoodScionTest extends BaseCardTest {

    @Test
    @DisplayName("Your spell targeting Elderwood Scion costs {2} less")
    void ownSpellTargetingSourceIsReduced() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ElderwoodScion());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, scion.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Your spell targeting another creature is not reduced")
    void ownSpellTargetingAnotherCreatureIsNotReduced() {
        harness.addToBattlefield(player1, new ElderwoodScion());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's spell targeting Elderwood Scion costs {2} more")
    void opponentSpellTargetingSourceIsTaxed() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ElderwoodScion());

        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, scion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's spell targeting another creature is not taxed")
    void opponentSpellTargetingAnotherCreatureIsNotTaxed() {
        harness.addToBattlefield(player1, new ElderwoodScion());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
