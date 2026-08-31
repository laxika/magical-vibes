package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MeldedMoxite.class, GrizzlyBears.class})
class MeldedMoxiteTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability discards a card and draws two cards")
    void acceptingEtbAbilityDiscardsAndDrawsTwo() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears drawnOne = new GrizzlyBears();
        GrizzlyBears drawnTwo = new GrizzlyBears();
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
        GrizzlyBears cardInHand = new GrizzlyBears();
        GrizzlyBears topCard = new GrizzlyBears();
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
        GrizzlyBears topCard = new GrizzlyBears();
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

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castMeldedMoxite() {
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
