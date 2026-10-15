package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirulentSwipe.class, GrizzlyBears.class, FountainOfYouth.class, NestInvader.class})
class VirulentSwipeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +2/+0 and deathtouch")
    void boostsTargetAndGrantsDeathtouch() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentSwipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("The boost and deathtouch wear off at cleanup")
    void effectWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirulentSwipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new VirulentSwipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void reboundWaitsForYourUpkeepAndCanTargetAnOpponentsCreatureForFree() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        VirulentSwipe card = new VirulentSwipe();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Virulent Swipe");
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(second.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(first.getPowerModifier()).isZero();
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Virulent Swipe");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOffer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        VirulentSwipe card = new VirulentSwipe();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Virulent Swipe");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    void illegalTargetPreventsResolutionAndRebound() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        VirulentSwipe card = new VirulentSwipe();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Virulent Swipe");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    void reboundWithNoLegalTargetLeavesCardExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        VirulentSwipe card = new VirulentSwipe();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Virulent Swipe");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}
