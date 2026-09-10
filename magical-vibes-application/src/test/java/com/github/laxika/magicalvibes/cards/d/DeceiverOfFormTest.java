package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeceiverOfForm.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class DeceiverOfFormTest extends BaseCardTest {

    @Test
    @DisplayName("May have other creatures you control copy the revealed creature")
    void copiesOtherControlledCreatures() {
        Permanent deceiver = addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(8);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Declining the copy leaves creatures unchanged")
    void decliningCopyLeavesCreaturesUnchanged() {
        addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("A noncreature reveal skips the copy choice and can still be put on the bottom")
    void noncreatureRevealSkipsCopy() {
        addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("bottom of your library");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(HillGiant.class);
    }

    @Test
    @DisplayName("Temporary copies end at cleanup")
    void copiesEndAtCleanup() {
        addDeceiver();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    private Permanent addDeceiver() {
        return addCreatureReady(player1, new DeceiverOfForm());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
