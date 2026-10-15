package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SparkDouble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoranDiscipleOfHistory.class, AdelizTheCinderWind.class, GrizzlyBears.class,
        Spellbook.class, SparkDouble.class})
class LoranDiscipleOfHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Loran returns a target artifact when it enters")
    void returnsArtifactWhenItEnters() {
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact, new GrizzlyBears()));
        harness.setHand(player1, List.of(new LoranDiscipleOfHistory()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Loran triggers for another legendary creature you control")
    void triggersForAnotherLegendaryCreature() {
        Spellbook artifact = new Spellbook();
        harness.addToBattlefield(player1, new LoranDiscipleOfHistory());
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(artifact.getId());
    }

    @Test
    @DisplayName("Loran does not trigger for a nonlegendary creature")
    void doesNotTriggerForNonlegendaryCreature() {
        Spellbook artifact = new Spellbook();
        harness.addToBattlefield(player1, new LoranDiscipleOfHistory());
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Loran has no trigger when the graveyard has no artifact card")
    void noArtifactNoTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LoranDiscipleOfHistory()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Loran, Disciple of History");
    }

    @Test
    @DisplayName("An opponent's legendary creature does not trigger Loran")
    void opponentLegendaryCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new LoranDiscipleOfHistory());
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.setHand(player2, List.of(new AdelizTheCinderWind()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Adeliz, the Cinder Wind");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Loran can target only artifacts in its controller's graveyard")
    void excludesOpponentGraveyard() {
        Spellbook ownArtifact = new Spellbook();
        Spellbook opponentArtifact = new Spellbook();
        harness.setGraveyard(player1, List.of(ownArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(new LoranDiscipleOfHistory()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownArtifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownArtifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("A nonlegendary copy of Loran triggers for its own entry")
    void nonlegendaryCopyTriggersForItself() {
        var loran = harness.addToBattlefieldAndReturn(player1, new LoranDiscipleOfHistory());
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new SparkDouble()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, loran.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
