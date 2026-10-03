package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelistOfTheMind.class, Forest.class, GrizzlyBears.class, Shock.class})
class DuelistOfTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards drawn this turn and toughness stays three")
    void powerTracksCardsDrawnThisTurn() {
        Permanent duelist = addCreatureReady(player1, new DuelistOfTheMind());

        assertThat(gqs.getEffectivePower(gd, duelist)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(3);

        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(2);

        gd.cardsDrawnThisTurn.put(player1.getId(), 4);
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Accepting the crime trigger draws a card, then discards a card")
    void acceptingCrimeTriggerDrawsThenDiscards() {
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.setHand(player1, new ArrayList<>(List.of(new Shock(), new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the crime trigger does not draw or discard")
    void decliningCrimeTriggerDoesNothing() {
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.setHand(player1, new ArrayList<>(List.of(new Shock(), new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.setHand(player1, new ArrayList<>(List.of(new Shock(), new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Targeting yourself is not a crime and does not consume the trigger")
    void selfTargetingDoesNotConsumeCrimeTrigger() {
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("An opponent's crime does not trigger Duelist of the Mind")
    void opponentCrimeDoesNotTrigger() {
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The drawn card can be discarded and still increases power")
    void discardingDrawnCardStillIncreasesPower() {
        Permanent duelist = harness.addToBattlefieldAndReturn(player1, new DuelistOfTheMind());
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power in hand uses its owner's draws rather than the opponent's draws")
    void powerInHandTracksOwnerDraws() {
        DuelistOfTheMind duelist = new DuelistOfTheMind();
        harness.setHand(player1, List.of(duelist));
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);
        gd.cardsDrawnThisTurn.put(player2.getId(), 5);

        assertThat(gqs.getEffectiveCardPower(gd, duelist)).isEqualTo(2);

        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        assertThat(gqs.getEffectiveCardPower(gd, duelist)).isZero();
    }

    @Test
    @DisplayName("Each Duelist has its own once-per-turn crime trigger")
    void eachDuelistTriggersIndependently() {
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.addToBattlefield(player1, new DuelistOfTheMind());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }
}
