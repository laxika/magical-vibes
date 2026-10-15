package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TeachByExample;
import com.github.laxika.magicalvibes.cards.t.TendThePests;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Jadzi.class, Forest.class, GrizzlyBears.class, Shock.class})
class JadziTest extends BaseCardTest {

    @Test
    @DisplayName("Magecraft puts a revealed land onto the battlefield without playing it")
    void magecraftPutsLandOntoBattlefield() {
        addCreatureReady(player1, new Jadzi());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand, new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, topLand.getName());
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Magecraft may cast a revealed nonland card for one mana")
    void magecraftCastsRevealedNonlandForOneMana() {
        addCreatureReady(player1, new Jadzi());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, topCard.getName());
    }

    @Test
    @DisplayName("Declining the revealed nonland card leaves it on top")
    void decliningRevealedNonlandLeavesItOnTop() {
        addCreatureReady(player1, new Jadzi());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("A revealed modal double-faced card can be cast using its back face")
    void revealedModalCardCanUseBackFace() {
        addCreatureReady(player1, new Jadzi());
        Jadzi topCard = new Jadzi();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "Journey to the Oracle");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Journey to the Oracle returns itself after putting in the eighth land")
    void journeyReturnsAfterPuttingInEighthLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        Jadzi card = new Jadzi();
        Forest land = new Forest();
        harness.setHand(player1, List.of(card, land, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(8);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(handCard -> handCard.getName().equals(card.getName()));
    }

    @Test
    @DisplayName("Jadzi's discard ability returns it to its owner's hand")
    void discardAbilityReturnsJadzi() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent jadzi = addCreatureReady(player1, new Jadzi());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals(jadzi.getCard().getName()));
    }

    @Test
    void journeyReturnsDuringItsResolutionWithoutASeparateTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Jadzi journey = new Jadzi();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(journey, discarded));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(journey);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void journeyBelowEightLandsDoesNotOfferDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Jadzi journey = new Jadzi();
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(journey, retained));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(journey);
    }

    @Test
    @CardUsed({TendThePests.class})
    void magecraftCannotCastASacrificeSpellWithoutACreature() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent jadzi = addCreatureReady(player1, new Jadzi());
        TendThePests top = new TendThePests();
        harness.setLibrary(player1, List.of(top, new Forest()));
        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(jadzi);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @CardUsed({TeachByExample.class})
    void magecraftTriggersForBothCastingAndCopyingAnInstant() {
        harness.setHand(player1, List.of(new TeachByExample(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0);
        addCreatureReady(player1, new Jadzi());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second, new GrizzlyBears()));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void journeyMayDeclineDiscardWithEightLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Jadzi journey = new Jadzi();
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(journey, retained));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(journey);
    }

    @Test
    void magecraftWithAnEmptyLibraryDoesNothing() {
        addCreatureReady(player1, new Jadzi());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Jadzi, Oracle of Arcavios");
    }
}
