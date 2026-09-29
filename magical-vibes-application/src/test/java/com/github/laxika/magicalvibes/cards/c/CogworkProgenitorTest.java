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

    private void beginEndStepMayChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
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
