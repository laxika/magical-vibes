package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HermesOverseerOfElpis.class, GrizzlyBears.class, Shock.class, SuntailHawk.class})
class HermesOverseerOfElpisTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a blue Bird with flying and vigilance")
    void noncreatureSpellCreatesBird() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bird, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Bird")
    void creatureSpellCreatesNoBird() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    @DisplayName("Attacking with one or more Birds scries two")
    void attackingWithBirdsScriesTwo() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player1, new SuntailHawk());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Attacking without a Bird does not scry")
    void attackingWithoutBirdDoesNotScry() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not create a Bird")
    void opponentNoncreatureSpellCreatesNoBird() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Bird")).isZero();
        assertThat(countPermanents(player2, "Bird")).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The Bird is created before the noncreature spell resolves")
    void birdIsCreatedBeforeSpellResolves() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
        harness.assertLife(player2, 20);
        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bird)).contains(CardSubtype.BIRD);
        assertThat(gqs.isCreature(gd, bird)).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple attacking Birds produce only one scry")
    void multipleAttackingBirdsScryOnlyOnce() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player1, new SuntailHawk());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the attacking Bird in response does not stop scry")
    void scryResolvesAfterAttackingBirdDies() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        Permanent bird = addCreatureReady(player1, new SuntailHawk());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(1));
        harness.castAndResolveInstant(player2, 0, bird.getId());
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Suntail Hawk");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent attacking with a Bird does not trigger scry")
    void opponentBirdAttackDoesNotScry() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player2, new SuntailHawk());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry two works with only one card in the library")
    void scryWithOneCardInLibrary() {
        harness.addToBattlefield(player1, new HermesOverseerOfElpis());
        addCreatureReady(player1, new SuntailHawk());
        GrizzlyBears onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }
}
