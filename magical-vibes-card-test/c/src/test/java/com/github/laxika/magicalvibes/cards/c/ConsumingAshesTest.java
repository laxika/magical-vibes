package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RooftopAssassin;
import com.github.laxika.magicalvibes.cards.v.VaultPlunderer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumingAshes.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        RooftopAssassin.class, VaultPlunderer.class})
class ConsumingAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature with mana value 3 or less and surveils 2")
    void exilesSmallCreatureAndSurveilsTwo() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card topCard = new Forest();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castConsumingAshes(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Exiles a creature with mana value greater than 3 without surveilling")
    void exilesLargeCreatureWithoutSurveil() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castConsumingAshes(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Air Elemental"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ConsumingAshes()));
        addManaForConsumingAshes();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void manaValueThreeAllowsKeepingBothCardsInEitherOrder() {
        Permanent target = addCreatureReady(player2, new VaultPlunderer());
        Card first = new Forest();
        Card second = new ConsumingAshes();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        castConsumingAshes(target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void manaValueFourDoesNotSurveilEvenWithCardsInLibrary() {
        Permanent target = addCreatureReady(player2, new RooftopAssassin());
        Card first = new Forest();
        Card second = new ConsumingAshes();
        harness.setLibrary(player1, List.of(first, second));

        castConsumingAshes(target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void canExileOwnCreatureAndPutBothSurveilledCardsInGraveyard() {
        Permanent target = addCreatureReady(player1, new VaultPlunderer());
        Card first = new Forest();
        Card second = new ConsumingAshes();
        harness.setLibrary(player1, List.of(first, second));

        castConsumingAshes(target);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilsAvailableCardWhenLibraryHasOnlyOneCard() {
        Permanent target = addCreatureReady(player2, new VaultPlunderer());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));

        castConsumingAshes(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotSurveilWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new VaultPlunderer());
        Card first = new Forest();
        Card second = new ConsumingAshes();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ConsumingAshes()));
        addManaForConsumingAshes();
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new ConsumingAshes()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Consuming Ashes");
        assertThat(gd.stack).isEmpty();
    }

    private void castConsumingAshes(Permanent target) {
        harness.setHand(player1, List.of(new ConsumingAshes()));
        addManaForConsumingAshes();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addManaForConsumingAshes() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
