package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
