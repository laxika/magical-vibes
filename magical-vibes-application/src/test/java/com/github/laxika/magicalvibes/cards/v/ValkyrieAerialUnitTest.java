package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValkyrieAerialUnit.class, Spellbook.class, GrizzlyBears.class})
class ValkyrieAerialUnitTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic cost and entering surveils two")
    void affinityAndEnterTheBattlefieldSurveil() {
        GameData gameData = harness.getGameData();
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new ValkyrieAerialUnit()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new ValkyrieAerialUnit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void affinityCountsArtifactCreaturesButNotNonartifactCreatures() {
        harness.addToBattlefield(player1, new ValkyrieAerialUnit());
        harness.addToBattlefield(player1, new ValkyrieAerialUnit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValkyrieAerialUnit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void excessAffinityDoesNotReduceBlueManaRequirements() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new ValkyrieAerialUnit());
        }
        harness.setHand(player1, List.of(new ValkyrieAerialUnit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void surveilCanReorderBothCardsWithoutTouchingTheRestOfTheLibrary() {
        Card first = new ValkyrieAerialUnit();
        Card second = new ValkyrieAerialUnit();
        Card third = new ValkyrieAerialUnit();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.enterBattlefieldAndReturn(player1, new ValkyrieAerialUnit());
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsIntoTheGraveyard() {
        Card first = new ValkyrieAerialUnit();
        Card second = new ValkyrieAerialUnit();
        Card third = new ValkyrieAerialUnit();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.enterBattlefieldAndReturn(player1, new ValkyrieAerialUnit());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void surveilWithOneCardLooksAtOnlyThatCard() {
        Card onlyCard = new ValkyrieAerialUnit();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.enterBattlefieldAndReturn(player1, new ValkyrieAerialUnit());
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void surveilWithAnEmptyLibraryCompletesWithoutAnInteraction() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new ValkyrieAerialUnit());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
