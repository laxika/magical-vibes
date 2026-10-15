package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarnTheGreatCreator.class, GrizzlyBears.class, IronMyr.class, Millstone.class, MindStone.class})
class KarnTheGreatCreatorTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents opponents from activating artifact abilities, including mana abilities")
    void preventsOpponentsArtifactAbilities() {
        addReadyKarn(5);
        Permanent ironMyr = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        ironMyr.setSummoningSick(false);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Does not prevent the controller from activating their artifact abilities")
    void allowsControllersArtifactAbilities() {
        addReadyKarn(5);
        Permanent ironMyr = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        ironMyr.setSummoningSick(false);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 animates a noncreature artifact with power and toughness equal to its mana value")
    void plusOneAnimatesNoncreatureArtifact() {
        Permanent karn = addReadyKarn(5);
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.activateAbility(player1, 0, 0, null, mindStone.getId());
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gqs.isCreature(gd, mindStone)).isTrue();
        assertThat(mindStone.getEffectivePower()).isEqualTo(2);
        assertThat(mindStone.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, mindStone)).isTrue();
    }

    @Test
    @DisplayName("+1 may be activated without choosing a target")
    void plusOneMayChooseNoTarget() {
        Permanent karn = addReadyKarn(5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 rejects a creature target")
    void plusOneRejectsCreatureTarget() {
        addReadyKarn(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IronMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("−2 offers artifact cards from outside the game and face-up exile")
    void minusTwoOffersArtifactsFromOutsideTheGameAndExile() {
        addReadyKarn(5);
        Card sideboardArtifact = new MindStone();
        Card sideboardCreature = new GrizzlyBears();
        Card exiledArtifact = new Millstone();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(sideboardArtifact, sideboardCreature)));
        gd.addToExile(player1.getId(), exiledArtifact);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.SearchOutsideGameOrExileCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                sideboardArtifact.getId(), exiledArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(exiledArtifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledArtifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledArtifact);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(sideboardArtifact, sideboardCreature);
    }

    @Test
    @DisplayName("Opposing artifacts remain animated throughout their controller's next turn")
    void opposingAnimationLastsUntilKarnsControllersNextTurn() {
        addReadyKarn(5);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
    }

    @Test
    @DisplayName("The controller's artifact animation expires at their next turn")
    void ownAnimationExpiresAtNextTurn() {
        addReadyKarn(5);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, artifact)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Prevents an opponent's nonmana artifact ability")
    void preventsNonmanaArtifactAbility() {
        addReadyKarn(5);
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Minus two puts the chosen outside-game artifact into hand and pays loyalty")
    void minusTwoRetrievesOutsideGameArtifact() {
        Permanent karn = addReadyKarn(5);
        Card artifact = new MindStone();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(artifact)));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Minus two excludes face-down exile and artifacts owned by opponents")
    void minusTwoExcludesIneligibleExiledCards() {
        addReadyKarn(5);
        Card eligible = new MindStone();
        Card faceDown = new Millstone();
        Card opponentsArtifact = new MindStone();
        Card nonartifact = new GrizzlyBears();
        gd.addToExile(player1.getId(), eligible);
        gd.addToExile(player1.getId(), faceDown, null, true);
        gd.addToExile(player2.getId(), opponentsArtifact);
        gd.addToExile(player1.getId(), nonartifact);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.SearchOutsideGameOrExileCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligible);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(eligible, faceDown, nonartifact);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentsArtifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Minus two resolves without eligible cards")
    void minusTwoWithNoEligibleCards() {
        Permanent karn = addReadyKarn(5);
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyKarn(int loyalty) {
        Permanent karn = harness.addToBattlefieldAndReturn(player1, new KarnTheGreatCreator());
        karn.setCounterCount(CounterType.LOYALTY, loyalty);
        karn.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return karn;
    }
}
