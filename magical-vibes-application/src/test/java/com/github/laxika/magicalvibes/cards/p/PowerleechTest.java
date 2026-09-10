package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DromarsAttendant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Powerleech.class, DromarsAttendant.class, GrizzlyBears.class, IcyManipulator.class, Ornithopter.class})
class PowerleechTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's artifact becoming tapped gains 1 life")
    void opponentArtifactBecomingTappedGainsLife() {
        harness.addToBattlefield(player1, new Powerleech());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        artifact.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, artifact));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An opponent's non-artifact becoming tapped does not trigger")
    void opponentNonArtifactBecomingTappedDoesNotTrigger() {
        harness.addToBattlefield(player1, new Powerleech());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's non-tap artifact ability gains 1 life")
    void opponentNonTapArtifactAbilityGainsLife() {
        harness.addToBattlefield(player1, new Powerleech());
        harness.addToBattlefield(player2, new DromarsAttendant());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An opponent's tap-cost artifact ability triggers only for the artifact becoming tapped")
    void opponentTapCostArtifactAbilityTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new Powerleech());
        Permanent manipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveStackFully();

        assertThat(manipulator.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Activating an own artifact ability does not trigger Powerleech")
    void ownArtifactAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new Powerleech());
        harness.addToBattlefield(player1, new DromarsAttendant());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
