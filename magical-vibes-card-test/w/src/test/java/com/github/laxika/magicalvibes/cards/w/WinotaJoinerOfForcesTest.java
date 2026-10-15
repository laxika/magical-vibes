package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.c.CheckpointOfficer;
import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinotaJoinerOfForces.class, EliteVanguard.class, GrizzlyBears.class,
        CheckpointOfficer.class, MosscoatGoriak.class, AmoeboidChangeling.class})
class WinotaJoinerOfForcesTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Human attacker puts a Human onto the battlefield tapped and attacking")
    void nonHumanAttackerPutsHumanOntoBattlefieldAttacking() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new GrizzlyBears());
        EliteVanguard human = new EliteVanguard();
        harness.setLibrary(player1, List.of(
                human,
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice libraryChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(libraryChoice).isNotNull();
        assertThat(libraryChoice.validCardIds()).containsExactly(human.getId());
        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));

        Permanent enteredHuman = findPermanent(player1, human.getName());
        assertThat(enteredHuman.isTapped()).isTrue();
        assertThat(enteredHuman.isAttacking()).isTrue();
        assertThat(enteredHuman.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, enteredHuman, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A Human attacker does not trigger Winota")
    void humanAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new EliteVanguard());
        harness.setLibrary(player1, List.of(new EliteVanguard()));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayDeclineHumanAndBottomAllSixCards() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new MosscoatGoriak());
        CheckpointOfficer human = new CheckpointOfficer();
        Card seventh = new MosscoatGoriak();
        List<Card> lookedAt = List.of(human, new MosscoatGoriak(), new MosscoatGoriak(),
                new MosscoatGoriak(), new MosscoatGoriak(), new MosscoatGoriak());
        List<Card> library = new ArrayList<>(lookedAt);
        library.add(seventh);
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            harness.handleMultipleCardsChosen(player1, List.of());
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
            assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                    .containsExactlyInAnyOrderElementsOf(lookedAt);
        });
    }

    @Test
    void cannotChooseHumanBelowTopSix() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new MosscoatGoriak());
        CheckpointOfficer seventh = new CheckpointOfficer();
        List<Card> topSix = List.of(new MosscoatGoriak(), new MosscoatGoriak(), new MosscoatGoriak(),
                new MosscoatGoriak(), new MosscoatGoriak(), new MosscoatGoriak());
        List<Card> library = new ArrayList<>(topSix);
        library.add(seventh);
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
            assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                    .containsExactlyInAnyOrderElementsOf(topSix);
        });
    }

    @Test
    void twoAttackersResolveSeparateGroupsOfSix() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new MosscoatGoriak());
        addCreatureReady(player1, new MosscoatGoriak());
        CheckpointOfficer first = new CheckpointOfficer();
        CheckpointOfficer second = new CheckpointOfficer();
        List<Card> library = new ArrayList<>();
        library.add(first);
        for (int i = 0; i < 5; i++) library.add(new MosscoatGoriak());
        library.add(second);
        for (int i = 0; i < 5; i++) library.add(new MosscoatGoriak());
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                    .validCardIds()).containsExactly(first.getId());
            harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                    .validCardIds()).containsExactly(second.getId());
            harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
            resolveAllTriggers();
            assertThat(findPermanents(player1, first.getName())).hasSize(2).allSatisfy(permanent -> {
                assertThat(permanent.isTapped()).isTrue();
                assertThat(permanent.isAttacking()).isTrue();
                assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
            });
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        });
    }

    @Test
    @CardUsed({AmoeboidChangeling.class})
    void creatureThatGainsHumanTypeDoesNotTrigger() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent attacker = addCreatureReady(player1, new MosscoatGoriak());
        harness.setLibrary(player1, List.of(new CheckpointOfficer()));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 1, 0, null, attacker.getId());
            resolveAllTriggers();
        });
        assertThat(gqs.hasEffectiveSubtype(gd, attacker, CardSubtype.HUMAN)).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(2));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        });
    }

    @Test
    @CardUsed({AmoeboidChangeling.class})
    void humanThatLosesCreatureTypesTriggers() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent attacker = addCreatureReady(player1, new CheckpointOfficer());
        CheckpointOfficer human = new CheckpointOfficer();
        harness.setLibrary(player1, List.of(human));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 1, 1, null, attacker.getId());
            resolveAllTriggers();
        });
        assertThat(gqs.hasEffectiveSubtype(gd, attacker, CardSubtype.HUMAN)).isFalse();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(2));
            resolveAllTriggers();
            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validCardIds()).containsExactly(human.getId());
            harness.handleMultipleCardsChosen(player1, List.of(human.getId()));
            assertThat(findPermanents(player1, human.getName())).hasSize(2);
        });
    }

    @Test
    void shortLibraryStillPutsHumanAttackingAndIndestructibleExpires() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new MosscoatGoriak());
        CheckpointOfficer human = new CheckpointOfficer();
        harness.setLibrary(player1, List.of(human));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            harness.handleMultipleCardsChosen(player1, List.of(human.getId()));
            Permanent entered = findPermanent(player1, human.getName());
            assertThat(entered.isTapped()).isTrue();
            assertThat(entered.isAttacking()).isTrue();
            assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(gqs.hasKeyword(gd, entered, Keyword.INDESTRUCTIBLE)).isTrue();
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        });

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, human.getName()), Keyword.INDESTRUCTIBLE))
                .isFalse();
    }

    @Test
    void emptyLibraryResolvesWithoutChoice() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new MosscoatGoriak());
        harness.setLibrary(player1, List.of());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        });
    }

    @Test
    void opponentNonHumanAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new WinotaJoinerOfForces());
        addCreatureReady(player2, new MosscoatGoriak());
        CheckpointOfficer human = new CheckpointOfficer();
        harness.setLibrary(player1, List.of(human));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(human);
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        });
    }
}
