package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BrainFreeze;
import com.github.laxika.magicalvibes.cards.f.FrozenSolid;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Metamorphose.class, BrainFreeze.class, FrozenSolid.class,
        Stabilizer.class, TempleOfTheFalseGod.class, ZombieCutthroat.class, JaceBeleren.class})
class MetamorphoseTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target permanent on top and offers its opponent a matching permanent card")
    void putsTargetOnTopAndOffersPermanentCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieCutthroat());
        Card libraryCard = new TempleOfTheFalseGod();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(
                new Stabilizer(), new FrozenSolid(), new TempleOfTheFalseGod(), new BrainFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Zombie Cutthroat");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getCard().getId(), libraryCard.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0, 1, 2);

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Stabilizer");
        harness.assertInHand(player2, "Frozen Solid");
        harness.assertInHand(player2, "Temple of the False God");
        harness.assertInHand(player2, "Brain Freeze");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent may decline to put a permanent onto the battlefield")
    void opponentMayDeclinePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieCutthroat());
        Card libraryCard = new TempleOfTheFalseGod();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new TempleOfTheFalseGod()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player2, "Zombie Cutthroat");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId())
                .isEqualTo(target.getCard().getId());
        harness.assertInHand(player2, "Temple of the False God");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZombieCutthroat());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not offer a planeswalker card from the opponent's hand")
    void doesNotOfferPlaneswalkerCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieCutthroat());
        harness.setLibrary(player2, List.of(new TempleOfTheFalseGod()));
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new TempleOfTheFalseGod(), new JaceBeleren(), new BrainFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(0);
    }

    @Test
    void opponentCanPutCreatureOntoBattlefieldWithoutPayingItsManaCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stabilizer());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new ZombieCutthroat()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Zombie Cutthroat");
        harness.assertNotInHand(player2, "Zombie Cutthroat");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCanPutLandOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stabilizer());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new TempleOfTheFalseGod()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Temple of the False God");
        harness.assertNotInHand(player2, "Temple of the False God");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenOpponentHasNoCardsInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieCutthroat());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId())
                .isEqualTo(target.getCard().getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotOfferPermanentWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieCutthroat());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new Stabilizer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Stabilizer");
        harness.assertNotOnBattlefield(player2, "Stabilizer");
        harness.assertInGraveyard(player1, "Metamorphose");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void auraStaysInHandWhenThereIsNothingLegalToEnchant() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZombieCutthroat());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new FrozenSolid()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player2, "Frozen Solid");
        harness.assertNotOnBattlefield(player2, "Frozen Solid");
        harness.assertNotInGraveyard(player2, "Frozen Solid");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentChoosesWhatAuraEnchantsAsItEnters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stabilizer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZombieCutthroat());
        harness.addToBattlefield(player2, new ZombieCutthroat());
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new FrozenSolid()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertOnBattlefield(player2, "Frozen Solid");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Frozen Solid"))
                .singleElement().extracting(Permanent::getAttachedTo).isEqualTo(creature.getId());
        harness.assertNotInHand(player2, "Frozen Solid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsBorrowedPermanentToOwnersLibraryButOffersItsControllerTheChoice() {
        Card borrowedCard = new ZombieCutthroat();
        borrowedCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, borrowedCard);
        Card libraryCard = new TempleOfTheFalseGod();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new Metamorphose()));
        harness.setHand(player2, List.of(new Stabilizer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsExactly(borrowedCard.getId(), libraryCard.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Stabilizer");
        harness.assertNotOnBattlefield(player2, "Zombie Cutthroat");
        assertThat(gd.stack).isEmpty();
    }
}
