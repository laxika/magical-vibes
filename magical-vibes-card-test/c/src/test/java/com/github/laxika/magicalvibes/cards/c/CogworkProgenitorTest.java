package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CogworkProgenitor.class, SolRing.class})
class CogworkProgenitorTest extends BaseCardTest {

    @Test
    void exilesAnotherArtifactYouControlAndPerpetuallyModifiesTheSoughtArtifact() {
        addCreatureReady(player1, new CogworkProgenitor());
        Permanent artifact = addCreatureReady(player1, new SolRing());
        Card sought = new SolRing();
        harness.setLibrary(player1, List.of(sought));

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .doesNotContain(artifact.getCard().getId());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(artifact.getCard().getId()));
        assertModifiedSoughtCard(sought);
    }

    @Test
    void exilesAnArtifactCardFromTheGraveyard() {
        addCreatureReady(player1, new CogworkProgenitor());
        Card graveyardArtifact = new SolRing();
        Card sought = new SolRing();
        harness.setGraveyard(player1, List.of(graveyardArtifact));
        harness.setLibrary(player1, List.of(sought));

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(graveyardArtifact.getId());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(graveyardArtifact.getId()));
        assertModifiedSoughtCard(sought);
    }

    @Test
    void mayAbilityCanBeDeclined() {
        addCreatureReady(player1, new CogworkProgenitor());
        Permanent artifact = addCreatureReady(player1, new SolRing());
        Card sought = new SolRing();
        harness.setLibrary(player1, List.of(sought));

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .contains(artifact.getCard().getId());
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(artifact.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).contains(sought.getId());
    }

    @Test
    void canExileTheSourceFromTheGraveyardIfItDiesAfterTriggering() {
        Permanent source = addCreatureReady(player1, new CogworkProgenitor());
        Card sought = new CogworkProgenitor();
        harness.setLibrary(player1, List.of(sought));

        beginEndStepTrigger();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(source.getCard().getId());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(source.getCard().getId()));
        assertModifiedSoughtCard(sought);
    }

    @Test
    void cannotExileItselfOnTheBattlefieldOrAnOpponentsArtifacts() {
        harness.setHand(player1, List.of());
        Permanent source = addCreatureReady(player1, new CogworkProgenitor());
        Permanent opponentArtifact = addCreatureReady(player2, new CogworkProgenitor());
        Card opponentGraveyardArtifact = new CogworkProgenitor();
        Card sought = new CogworkProgenitor();
        harness.setGraveyard(player2, List.of(opponentGraveyardArtifact));
        harness.setLibrary(player1, List.of(sought));

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentGraveyardArtifact);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(sought);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void choosesBetweenBattlefieldAndGraveyardArtifactsAtResolution() {
        addCreatureReady(player1, new CogworkProgenitor());
        Permanent battlefieldArtifact = addCreatureReady(player1, new SolRing());
        Card graveyardArtifact = new SolRing();
        Card sought = new CogworkProgenitor();
        harness.setGraveyard(player1, List.of(graveyardArtifact));
        harness.setLibrary(player1, List.of(sought));

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ArtifactPermanentOrGraveyardCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(battlefieldArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(graveyardArtifact.getId()));
        assertModifiedSoughtCard(sought);
    }

    @Test
    void stillExilesTheChosenArtifactWhenTheLibraryIsEmpty() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new CogworkProgenitor());
        Card artifact = new CogworkProgenitor();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of());

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(artifact.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void perpetualModificationSurvivesEnteringAndLeavingTheBattlefield() {
        addCreatureReady(player1, new CogworkProgenitor());
        Card artifact = new CogworkProgenitor();
        Card sought = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(sought));

        beginEndStepMayChoice();
        harness.handleMayAbilityChosen(player1, true);
        Card modified = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(sought.getId())).findFirst().orElseThrow();
        gd.playerHands.get(player1.getId()).remove(modified);
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, modified);

        assertThat(gqs.isCreature(gd, permanent)).isTrue();
        assertThat(gqs.isArtifact(gd, permanent)).isTrue();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, permanent));
        assertModifiedSoughtCard(sought);
    }

    private void beginEndStepMayChoice() {
        beginEndStepTrigger();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void beginEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }

    private void assertModifiedSoughtCard(Card sought) {
        Card modified = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(sought.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(modified.getType()).isEqualTo(CardType.CREATURE);
        assertThat(modified.getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(modified.getSubtypes()).contains(CardSubtype.GNOME);
        assertThat(modified.getPower()).isEqualTo(1);
        assertThat(modified.getToughness()).isEqualTo(1);
    }
}
