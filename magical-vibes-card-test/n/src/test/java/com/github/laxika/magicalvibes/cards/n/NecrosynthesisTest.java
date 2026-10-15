package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necrosynthesis.class, GrizzlyBears.class, HillGiant.class, Forest.class, Mountain.class,
        Island.class})
class NecrosynthesisTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets a +1/+1 counter when another creature dies")
    void putsCounterOnEnchantedCreatureWhenAnotherCreatureDies() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(enchanted);

        other.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature retains the granted trigger when it dies with another creature")
    void grantedTriggerUsesLastKnownStateForSimultaneousDeaths() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(enchanted);
        enchanted.setMarkedDamage(2);
        other.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("When enchanted creature dies, look at cards equal to its power and keep one")
    void looksAtCardsEqualToEnchantedCreaturePower() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachAura(enchanted);
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, mountain, island));

        enchanted.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(forest, mountain, island);

        harness.handleMultipleCardsChosen(player1, List.of(mountain.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Aura controller uses their own library when an opponent's enchanted creature dies")
    void auraControllerUsesOwnLibrary() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(enchanted);
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, mountain, island));
        Card opposingCard = new Forest();
        harness.setLibrary(player2, List.of(opposingCard));

        enchanted.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(forest, mountain);
        harness.handleMultipleCardsChosen(player1, List.of(mountain.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingCard);
    }

    @Test
    @DisplayName("Death trigger uses power including counters accumulated from earlier deaths")
    void deathTriggerIncludesAccumulatedCounters() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(enchanted);
        other.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card island = new Island();
        Card unseen = new Forest();
        harness.setLibrary(player1, List.of(forest, mountain, island, unseen));

        enchanted.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(forest, mountain, island);
        harness.handleMultipleCardsChosen(player1, List.of(mountain.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(unseen, forest, island);
    }

    @Test
    @DisplayName("A library with only one card puts that card into hand even when power is greater")
    void shortLibraryPutsOnlyCardIntoHand() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachAura(enchanted);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        enchanted.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Necrosynthesis());
        aura.setAttachedTo(creature.getId());
    }
}
