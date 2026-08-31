package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagingBattleMouse.class, Forest.class, GrizzlyBears.class})
class RagingBattleMouseTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell each turn does not get the reduction")
    void firstSpellIsNotReduced() {
        harness.addToBattlefield(player1, new RagingBattleMouse());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the second spell each turn costs {1} less")
    void onlySecondSpellIsReduced() {
        harness.addToBattlefield(player1, new RagingBattleMouse());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Celebration boosts a target creature you control")
    void celebrationBoostsTargetCreature() {
        castRagingBattleMouse();
        Permanent bear = castGrizzlyBears();

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Celebration does not trigger without two nonland permanents")
    void celebrationDoesNotTriggerWithoutTwoNonlandPermanents() {
        castRagingBattleMouse();
        harness.addToBattlefield(player1, new Forest());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Celebration only offers creatures you control")
    void celebrationOnlyOffersCreaturesYouControl() {
        castRagingBattleMouse();
        Permanent ownBear = castGrizzlyBears();
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownBear.getId())
                .doesNotContain(opposingBear.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRagingBattleMouse() {
        harness.setHand(player1, List.of(new RagingBattleMouse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
