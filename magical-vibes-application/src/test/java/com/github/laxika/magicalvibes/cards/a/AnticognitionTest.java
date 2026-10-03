package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.cards.r.RelicAmulet;
import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
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

@CardUsed({Anticognition.class, TazeemRoilmage.class, RelicAmulet.class, JaceMirrorMage.class})
class AnticognitionTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature spell")
    void canTargetCreatureSpell() {
        TazeemRoilmage creature = castCreatureSpell(2);
        castAnticognition(creature);

        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetNoncreatureSpell() {
        RelicAmulet artifact = new RelicAmulet();
        harness.setHand(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Anticognition()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a creature spell unless its controller pays {2}")
    void countersUnlessControllerPays() {
        TazeemRoilmage creature = castCreatureSpell(4);
        castAndResolveAnticognition(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, creature.getName());
    }

    @Test
    @DisplayName("Counters and scries 2 when an opponent has eight graveyard cards")
    void countersAndScriesAtGraveyardThreshold() {
        harness.setGraveyard(player1, graveyardOfSize(8));
        harness.setLibrary(player2, List.of(new RelicAmulet(), new RelicAmulet(), new RelicAmulet()));

        TazeemRoilmage creature = castCreatureSpell(2);
        castAndResolveAnticognition(creature);

        harness.assertInGraveyard(player1, creature.getName());
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

        TazeemRoilmage creature = castCreatureSpell(4);
        castAndResolveAnticognition(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Tazeem Roilmage");
    }

    @Test
    @DisplayName("Counters a planeswalker spell when its controller cannot pay")
    void countersPlaneswalkerSpell() {
        JaceMirrorMage jace = new JaceMirrorMage();
        harness.setHand(player1, List.of(jace));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        castAndResolveAnticognition(jace);

        harness.assertInGraveyard(player1, jace.getName());
        harness.assertNotOnBattlefield(player1, jace.getName());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Seven graveyard cards do not grant scry even when countering adds the eighth")
    void sevenCardsDoNotEnableScry() {
        harness.setGraveyard(player1, graveyardOfSize(7));
        harness.setLibrary(player2, List.of(new RelicAmulet(), new RelicAmulet()));
        TazeemRoilmage creature = castCreatureSpell(2);
        castAndResolveAnticognition(creature);

        harness.assertInGraveyard(player1, creature.getName());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The threshold branch counters without offering payment despite available mana")
    void thresholdIgnoresAvailablePayment() {
        harness.setGraveyard(player1, graveyardOfSize(8));
        RelicAmulet first = new RelicAmulet();
        RelicAmulet second = new RelicAmulet();
        harness.setLibrary(player2, List.of(first, second));
        TazeemRoilmage creature = castCreatureSpell(4);
        castAndResolveAnticognition(creature);

        harness.assertInGraveyard(player1, creature.getName());
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reaching eight graveyard cards before resolution enables counter and scry")
    void thresholdCheckedAtResolutionWhenGraveyardGrows() {
        harness.setGraveyard(player1, graveyardOfSize(7));
        harness.setLibrary(player2, List.of(new RelicAmulet(), new RelicAmulet()));
        TazeemRoilmage creature = castCreatureSpell(4);
        castAnticognition(creature);
        harness.setGraveyard(player1, graveyardOfSize(8));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, creature.getName());
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Dropping below eight graveyard cards before resolution permits payment")
    void thresholdCheckedAtResolutionWhenGraveyardShrinks() {
        harness.setGraveyard(player1, graveyardOfSize(8));
        TazeemRoilmage creature = castCreatureSpell(4);
        castAnticognition(creature);
        harness.setGraveyard(player1, graveyardOfSize(7));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, creature.getName());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private TazeemRoilmage castCreatureSpell(int mana) {
        TazeemRoilmage creature = new TazeemRoilmage();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, mana);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return creature;
    }

    private void castAnticognition(Card target) {
        harness.setHand(player2, List.of(new Anticognition()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, target.getId());
    }

    private void castAndResolveAnticognition(Card target) {
        harness.setHand(player2, List.of(new Anticognition()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new RelicAmulet());
        }
        return cards;
    }
}
