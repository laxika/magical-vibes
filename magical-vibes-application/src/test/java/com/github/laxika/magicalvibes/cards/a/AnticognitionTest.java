package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Anticognition.class, GrizzlyBears.class, Millstone.class, Spellbook.class})
class AnticognitionTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature spell")
    void canTargetCreatureSpell() {
        GrizzlyBears bears = castCreatureSpell(2);
        castAnticognition(bears);

        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetNoncreatureSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Anticognition()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a creature spell unless its controller pays {2}")
    void countersUnlessControllerPays() {
        GrizzlyBears bears = castCreatureSpell(4);
        castAnticognition(bears);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, bears.getName());
    }

    @Test
    @DisplayName("Counters and scries 2 when an opponent has eight graveyard cards")
    void countersAndScriesAtGraveyardThreshold() {
        harness.setGraveyard(player1, graveyardOfSize(8));
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook(), new Spellbook()));

        GrizzlyBears bears = castCreatureSpell(2);
        castAnticognition(bears);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, bears.getName());
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own graveyard does not enable the threshold branch")
    void ownGraveyardDoesNotEnableThresholdBranch() {
        harness.setGraveyard(player2, graveyardOfSize(8));

        GrizzlyBears bears = castCreatureSpell(4);
        castAnticognition(bears);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private GrizzlyBears castCreatureSpell(int mana) {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, mana);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return bears;
    }

    private void castAnticognition(Card target) {
        harness.setHand(player2, List.of(new Anticognition()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, target.getId());
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }
}
