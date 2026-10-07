package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WordsOfWind;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TezzeretArtificeMaster.class, GrizzlyBears.class, IcyManipulator.class,
        Mountain.class, Divination.class, WordsOfWind.class})
class TezzeretArtificeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a 1/1 flying Thopter artifact creature token")
    void plusOneCreatesThopter() {
        Permanent tezzeret = addReadyTezzeret(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(1);
        Permanent thopter = thopters.getFirst();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("0 draws one card with fewer than three artifacts")
    void zeroDrawsOneWithoutThreeArtifacts() {
        addReadyTezzeret(player1, 5);
        addArtifacts(player1, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("0 draws two cards with three or more artifacts")
    void zeroDrawsTwoWithThreeArtifacts() {
        addReadyTezzeret(player1, 5);
        addArtifacts(player1, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("−9 emblem searches for a permanent card onto the battlefield at the controller's end step")
    void ultimateEmblemSearchesAtEndStep() {
        Permanent tezzeret = addReadyTezzeret(player1, 9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));

        advanceIntoEndStep(player1);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The emblem does not trigger at the opponent's end step")
    void emblemDoesNotTriggerOnOpponentsEndStep() {
        addReadyTezzeret(player1, 9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceIntoEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("0 checks artifact count when it resolves")
    void zeroUsesArtifactCountAtResolution() {
        addReadyTezzeret(player1, 5);
        addArtifacts(player1, 2);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Opponent's artifacts do not count toward drawing two")
    void zeroIgnoresOpponentsArtifacts() {
        addReadyTezzeret(player1, 5);
        addArtifacts(player1, 2);
        addArtifacts(player2, 3);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Replacing a draw and losing metalcraft does not add another draw")
    void replacedDrawDoesNotEnableAdditionalDraw() {
        addReadyTezzeret(player1, 5);
        addArtifacts(player1, 3);
        harness.addToBattlefield(player1, new WordsOfWind());
        harness.addToBattlefield(player2, new Mountain());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).get(1);
        Mountain drawnCard = new Mountain();
        Mountain remainingCard = new Mountain();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard, new Mountain()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 4, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        harness.assertInHand(player2, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(artifact.getCard(), drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).first().isSameAs(remainingCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Emblem can find a land but cannot find a sorcery")
    void emblemFiltersNonpermanentsAndPutsLandOntoBattlefieldUntapped() {
        addReadyTezzeret(player1, 9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tezzeret, Artifice Master");
        Divination sorcery = new Divination();
        Mountain land = new Mountain();
        harness.setLibrary(player1, List.of(sorcery, land));

        advanceIntoEndStep(player1);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(land);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanent(player1, "Mountain").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorcery);
    }

    @Test
    @DisplayName("Emblem permits failing to find a permanent in the library")
    void emblemCanFailToFind() {
        addReadyTezzeret(player1, 9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Mountain land = new Mountain();
        harness.setLibrary(player1, List.of(land));

        advanceIntoEndStep(player1);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new IcyManipulator());
        }
    }

    /** Advances {@code activePlayer} into their end step so the step's triggers are collected. */
    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private Permanent addReadyTezzeret(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TezzeretArtificeMaster());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
