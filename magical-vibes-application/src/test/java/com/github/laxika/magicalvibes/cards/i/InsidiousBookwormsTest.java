package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DeathSpark;
import com.github.laxika.magicalvibes.cards.f.FyndhornDruid;
import com.github.laxika.magicalvibes.cards.k.KjeldoranHomeGuard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InsidiousBookworms.class, DeathSpark.class, FyndhornDruid.class, KjeldoranHomeGuard.class})
class InsidiousBookwormsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{B} makes the targeted player discard a card at random")
    void payingMakesTargetPlayerDiscard() {
        harness.addToBattlefield(player1, new InsidiousBookworms());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        killBookworms();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Player2 cast Death Spark and had 2 other cards left; one is discarded at random.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Death Spark"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment discards nothing")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new InsidiousBookworms());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        killBookworms();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Death Spark"))
                .isEmpty();
    }

    @Test
    @DisplayName("The controller may target themselves — the ability says target player")
    void controllerIsALegalTarget() {
        harness.addToBattlefield(player1, new InsidiousBookworms());
        harness.setHand(player1, List.of(new KjeldoranHomeGuard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        killBookworms();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Kjeldoran Home Guard");
    }

    @Test
    @DisplayName("Accepting without enough mana does not resolve the optional effect")
    void acceptingWithoutEnoughManaDoesNothing() {
        harness.addToBattlefield(player1, new InsidiousBookworms());

        killBookworms();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Death Spark"))
                .isEmpty();
    }

    @Test
    @DisplayName("Paying does nothing when the targeted player has no cards")
    void emptyTargetHandDiscardsNothing() {
        harness.addToBattlefield(player1, new InsidiousBookworms());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        killBookworms(new DeathSpark());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Death Spark"))
                .isEmpty();
    }

    /** Player2 casts Death Spark on the Bookworms on their own turn. */
    private void killBookworms() {
        killBookworms(new DeathSpark(), new FyndhornDruid(), new KjeldoranHomeGuard());
    }

    private void killBookworms(Card... opponentHand) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(opponentHand));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bookwormsId = harness.getPermanentId(player1, "Insidious Bookworms");
        harness.castAndResolveInstant(player2, 0, bookwormsId);
    }
}
