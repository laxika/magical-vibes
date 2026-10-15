package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BurglarRat;
import com.github.laxika.magicalvibes.cards.i.IzoniThousandEyed;
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

@CardUsed({PriceOfFame.class, BurglarRat.class, IzoniThousandEyed.class, Forest.class})
class PriceOfFameTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and surveils two")
    void destroysTargetCreatureAndSurveilsTwo() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        Card topCard = new BurglarRat();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Burglar Rat");
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Costs {1}{B} when targeting a legendary creature")
    void reducedCostWhenTargetingLegendaryCreature() {
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player2, new IzoniThousandEyed());
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, legendaryCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Izoni, Thousand-Eyed");
    }

    @Test
    @DisplayName("Requires the full cost when targeting a nonlegendary creature")
    void fullCostWhenTargetingNonlegendaryCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canKeepBothSurveilledCardsInReverseOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        Card first = new BurglarRat();
        Card second = new Forest();
        Card third = new PriceOfFame();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
    }

    @Test
    void canPutBothSurveilledCardsIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        Card first = new BurglarRat();
        Card second = new Forest();
        Card third = new PriceOfFame();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    void surveilsOnlyAvailableCardInOneCardLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
        harness.assertInGraveyard(player2, "Burglar Rat");
    }

    @Test
    void destroysCreatureWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Burglar Rat");
        harness.assertInGraveyard(player1, "Price of Fame");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotSurveilWhenTargetWasDestroyedInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurglarRat());
        Card first = new Forest();
        Card second = new BurglarRat();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new PriceOfFame()));
        harness.setHand(player2, List.of(new PriceOfFame()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Price of Fame");
        harness.assertInGraveyard(player2, "Burglar Rat");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
