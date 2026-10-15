package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CookingCampsite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidequestCatchAFish.class, CookingCampsite.class, GrizzlyBears.class, Millstone.class, Shock.class})
class SidequestCatchAFishTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an artifact or creature revealed from the top into hand, creates Food, and transforms")
    void matchingCreatureTransformsAndCreatesFood() {
        Permanent source = addSidequest(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(source.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Transforms for a noncreature artifact revealed from the top")
    void matchingArtifactTransforms() {
        Permanent source = addSidequest(player1);
        Card topCard = new Millstone();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(source.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Declining the reveal leaves the matching card on top and does not transform")
    void decliningRevealDoesNotTransform() {
        Permanent source = addSidequest(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(source.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("A non-artifact, noncreature top card does not offer a reveal")
    void nonmatchingTopCardDoesNotTransform() {
        Permanent source = addSidequest(player1);
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(source.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Cooking Campsite sacrifices an artifact and puts counters on all controlled creatures")
    void campsiteSacrificesArtifactAndCountersCreatures() {
        Permanent campsite = addTransformedCampsite(player1);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(player1, campsite), 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherArtifact);
    }

    @Test
    @DisplayName("An empty library creates no Food and does not transform")
    void emptyLibraryDoesNotTransform() {
        Permanent source = addSidequest(player1);
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());

        assertThat(source.isTransformed()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("The opponent's upkeep does not trigger the sidequest")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent source = addSidequest(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(source.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Food created by the sidequest can be sacrificed for three life")
    void createdFoodCanBeSacrificedForLife() {
        addSidequest(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, food), 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Cooking Campsite taps for white mana immediately without using the stack")
    void campsiteProducesWhiteMana() {
        Permanent campsite = addTransformedCampsite(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(player1, campsite), 0, null, null);

        assertThat(campsite.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cooking Campsite counters only its controller's creatures")
    void campsiteDoesNotCounterOpposingCreaturesOrNoncreatures() {
        Permanent campsite = addTransformedCampsite(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent remainingArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(player1, campsite), 1, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(campsite.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(remainingArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(campsite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cooking Campsite cannot activate its counter ability during upkeep")
    void campsiteCounterAbilityRequiresMainPhase() {
        Permanent campsite = addTransformedCampsite(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, campsite), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(campsite.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Cooking Campsite cannot activate its counter ability on an opponent's turn")
    void campsiteCounterAbilityRequiresControllersTurn() {
        Permanent campsite = addTransformedCampsite(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, campsite), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(campsite.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Looking at a nonmatching top card shows its identity only to the controller")
    void nonmatchingTopCardIsShownPrivately() {
        addSidequest(player1);
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.clearMessages();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anySatisfy(message -> assertThat(message).contains("Shock"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gameLogContains("Shock")).isFalse();
    }

    private Permanent addSidequest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SidequestCatchAFish());
    }

    private Permanent addTransformedCampsite(Player player) {
        SidequestCatchAFish front = new SidequestCatchAFish();
        Permanent campsite = harness.addToBattlefieldAndReturn(player, front);
        campsite.setCard(front.getBackFaceCard());
        campsite.setTransformed(true);
        campsite.setSummoningSick(false);
        return campsite;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
