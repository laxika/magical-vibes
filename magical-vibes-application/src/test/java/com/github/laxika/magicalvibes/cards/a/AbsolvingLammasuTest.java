package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbsolvingLammasu.class, GrizzlyBears.class, Terror.class})
class AbsolvingLammasuTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, its controller gains 3 life and may suspect an opponent's creature")
    void deathTriggerGainsLifeAndSuspectsTarget() {
        harness.addToBattlefield(player1, new AbsolvingLammasu());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID lammasuId = harness.getPermanentId(player1, "Absolving Lammasu");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int lifeBefore = gd.getLife(player1.getId());

        killLammasu(lammasuId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bearsId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(bears.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, com.github.laxika.magicalvibes.model.Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Entering the battlefield makes all suspected creatures no longer suspected")
    void enteringClearsSuspectedCreatures() {
        harness.addToBattlefield(player1, new AbsolvingLammasu());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID lammasuId = harness.getPermanentId(player1, "Absolving Lammasu");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        killLammasu(lammasuId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Grizzly Bears").isSuspected()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AbsolvingLammasu()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Grizzly Bears").isSuspected()).isFalse();
        assertThat(bls.canBlock(gd, findPermanent(player2, "Grizzly Bears"))).isTrue();
    }

    @Test
    @DisplayName("The death trigger can resolve without choosing a target")
    void deathTriggerCanDeclineTarget() {
        harness.addToBattlefield(player1, new AbsolvingLammasu());
        UUID lammasuId = harness.getPermanentId(player1, "Absolving Lammasu");
        int lifeBefore = gd.getLife(player1.getId());

        killLammasu(lammasuId);
        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void killLammasu(UUID lammasuId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, lammasuId);
        harness.passBothPriorities();
    }
}
