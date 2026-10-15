package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KeyToTheCity;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.cards.s.ServoExhibition;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OvalchaseDaredevil.class, Forest.class, PrakhataPillarBug.class,
        KeyToTheCity.class, ServoExhibition.class})
class OvalchaseDaredevilTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Accepting the artifact trigger returns Ovalchase Daredevil from graveyard to hand")
    void artifactReturnsFromGraveyardOnAccept() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player1);

        harness.setHand(player1, List.of(new PrakhataPillarBug()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(daredevil);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(daredevil);
    }

    @Test
    @DisplayName("Declining the artifact trigger keeps Ovalchase Daredevil in the graveyard")
    void artifactDeclineKeepsDaredevilInGraveyard() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player1);

        harness.setHand(player1, List.of(new PrakhataPillarBug()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(daredevil);
    }

    @Test
    @DisplayName("A nonartifact permanent entering does not trigger")
    void nonartifactPermanentDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new OvalchaseDaredevil()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ovalchase Daredevil");
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger")
    void opponentArtifactDoesNotTrigger() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player2);

        harness.setHand(player2, List.of(new PrakhataPillarBug()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void daredevilOnBattlefieldOrInHandDoesNotTrigger() {
        addCreatureReady(player1, new OvalchaseDaredevil());
        harness.setHand(player1, List.of(new PrakhataPillarBug(), new OvalchaseDaredevil()));
        prepareMain(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Ovalchase Daredevil");
        harness.assertOnBattlefield(player1, "Ovalchase Daredevil");
    }

    @Test
    void eachCopyHasAnIndependentOptionalReturn() {
        OvalchaseDaredevil first = new OvalchaseDaredevil();
        OvalchaseDaredevil second = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(first, second));
        prepareMain(player1);
        harness.setHand(player1, List.of(new PrakhataPillarBug()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noncreatureArtifactAlsoTriggersReturn() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        prepareMain(player1);
        harness.setHand(player1, List.of(new KeyToTheCity()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(daredevil);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    void permanentMadeArtifactByLatticeTriggersReturn() {
        OvalchaseDaredevil graveyardCard = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.addToBattlefield(player1, new MycosynthLattice());
        prepareMain(player1);
        harness.setHand(player1, List.of(new OvalchaseDaredevil()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void oldTriggerCannotReturnDaredevilAfterItLeavesAndReentersGraveyard() {
        OvalchaseDaredevil daredevil = new OvalchaseDaredevil();
        harness.setGraveyard(player1, List.of(daredevil));
        addCreatureReady(player1, new KeyToTheCity());
        prepareMain(player1);
        harness.setHand(player1, List.of(new ServoExhibition()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(daredevil);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(daredevil);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(daredevil);
        assertThat(gd.stack).isEmpty();
    }
}
