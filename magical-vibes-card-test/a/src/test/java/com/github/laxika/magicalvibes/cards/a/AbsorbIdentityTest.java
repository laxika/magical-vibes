package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
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

@CardUsed({AbsorbIdentity.class, GrizzlyBears.class, HillGiant.class, Island.class, WoodlandChangeling.class})
class AbsorbIdentityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and may make your Shapeshifters copies of it")
    void returnsCreatureAndCopiesControlledShapeshifters() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent shapeshifter = addCreatureReady(player1, new WoodlandChangeling());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentShapeshifter = addCreatureReady(player2, new WoodlandChangeling());

        castAbsorbIdentity(target);

        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(shapeshifter.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, shapeshifter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shapeshifter)).isEqualTo(3);
        assertThat(otherCreature.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(opponentShapeshifter.getCard().getName()).isEqualTo("Woodland Changeling");
    }

    @Test
    @DisplayName("Declining the copy option leaves Shapeshifters unchanged")
    void decliningCopyOptionLeavesShapeshiftersUnchanged() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent shapeshifter = addCreatureReady(player1, new WoodlandChangeling());

        castAbsorbIdentity(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(shapeshifter.getCard().getName()).isEqualTo("Woodland Changeling");
        harness.assertInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Temporary copies revert at cleanup")
    void copiesRevertAtCleanup() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent shapeshifter = addCreatureReady(player1, new WoodlandChangeling());

        castAbsorbIdentity(target);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(shapeshifter.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapeshifter.getCard().getName()).isEqualTo("Woodland Changeling");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new AbsorbIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAbsorbIdentity(Permanent target) {
        harness.setHand(player1, List.of(new AbsorbIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
