package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeldedMoxite.class})
class MeldedMoxiteTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability discards a card and draws two cards")
    void acceptingEtbAbilityDiscardsAndDrawsTwo() {
        MeldedMoxite discarded = new MeldedMoxite();
        MeldedMoxite drawnOne = new MeldedMoxite();
        MeldedMoxite drawnTwo = new MeldedMoxite();
        harness.setHand(player1, List.of(new MeldedMoxite(), discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addCastingMana();

        castMeldedMoxite();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("Declining the ETB ability does not discard or draw")
    void decliningEtbAbilityDoesNothing() {
        MeldedMoxite cardInHand = new MeldedMoxite();
        MeldedMoxite topCard = new MeldedMoxite();
        harness.setHand(player1, List.of(new MeldedMoxite(), cardInHand));
        harness.setLibrary(player1, List.of(topCard));
        addCastingMana();

        castMeldedMoxite();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting the ETB ability with no card in hand does nothing")
    void acceptingEtbAbilityWithNoCardDoesNothing() {
        MeldedMoxite topCard = new MeldedMoxite();
        harness.setHand(player1, List.of(new MeldedMoxite()));
        harness.setLibrary(player1, List.of(topCard));
        addCastingMana();

        castMeldedMoxite();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The activated ability sacrifices Melded Moxite and creates a tapped Robot")
    void activatedAbilitySacrificesAndCreatesTappedRobot() {
        harness.addToBattlefield(player1, new MeldedMoxite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Melded Moxite");
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.isTapped()).isTrue();
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
    }

    @Test
    @DisplayName("The discard and draw happen during the same ETB resolution")
    void drawsBeforePlayersReceivePriorityAfterDiscard() {
        MeldedMoxite discarded = new MeldedMoxite();
        MeldedMoxite drawnOne = new MeldedMoxite();
        MeldedMoxite drawnTwo = new MeldedMoxite();
        harness.setHand(player1, List.of(new MeldedMoxite(), discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addCastingMana();

        castMeldedMoxite();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a tapped Moxite pays the cost before the Robot is created")
    void tappedMoxiteIsSacrificedBeforeAbilityResolves() {
        Permanent moxite = harness.addToBattlefieldAndReturn(player1, new MeldedMoxite());
        moxite.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Melded Moxite");
        harness.assertInGraveyard(player1, "Melded Moxite");
        harness.assertNotOnBattlefield(player1, "Robot");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(findPermanent(player1, "Robot").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Robot");
    }

    @Test
    @DisplayName("The ETB draw still resolves after Moxite is sacrificed")
    void etbAbilityResolvesAfterSourceIsSacrificed() {
        MeldedMoxite discarded = new MeldedMoxite();
        MeldedMoxite drawnOne = new MeldedMoxite();
        MeldedMoxite drawnTwo = new MeldedMoxite();
        harness.setHand(player1, List.of(new MeldedMoxite(), discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addCastingMana();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        harness.assertNotOnBattlefield(player1, "Melded Moxite");
        assertThat(findPermanents(player1, "Robot")).hasSize(1);
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castMeldedMoxite() {
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
