package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CavalierOfFlame.class, GreenwoodSentinel.class, Forest.class, Mountain.class, ChandraNovicePyromancer.class})
class CavalierOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability boosts and grants haste to creatures you control until end of turn")
    void activatedAbilityBoostsOwnCreaturesAndGrantsHaste() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(7);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(6);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Enters by discarding any number of cards and drawing that many")
    void entersWithRummage() {
        Card discardOne = new GreenwoodSentinel();
        Card discardTwo = new GreenwoodSentinel();
        Card kept = new GreenwoodSentinel();
        Card drawOne = new Forest();
        Card drawTwo = new Mountain();
        harness.setLibrary(player1, List.of(drawOne, drawTwo));
        harness.setHand(player1, List.of(new CavalierOfFlame(), discardOne, discardTwo, kept));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(kept, drawOne, drawTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardOne, discardTwo);
    }

    @Test
    @DisplayName("Death deals damage equal to lands in its controller's graveyard to opponents and their planeswalkers")
    void deathDamagesOpponentsAndTheirPlaneswalkersBasedOnGraveyardLands() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain(), new GreenwoodSentinel()));
        cavalier.setMarkedDamage(6);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void activatedAbilityGrantsHasteToCavalierItself() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cavalier.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void repeatedActivationsStackAndDoNotAffectCreaturesEnteringLater() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThat(cavalier.getEffectivePower()).isEqualTo(8);
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void entersCanDiscardZeroWithoutDrawing() {
        Card kept = new GreenwoodSentinel();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.enterBattlefieldAndReturn(player1, new CavalierOfFlame());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void entersWithEmptyHandDoesNotDraw() {
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        harness.enterBattlefieldAndReturn(player1, new CavalierOfFlame());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void deathDoesNotDamageItsControllersPlaneswalkerOrCreatures() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent ownPlaneswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNovicePyromancer());
        Permanent opposingPlaneswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain()));
        cavalier.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void deathCountsControllerLandsAtResolutionRatherThanWhenItTriggers() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest(), new Mountain(), new Forest()));
        cavalier.setMarkedDamage(5);
        harness.runStateBasedActions();
        gd.playerGraveyards.get(player1.getId()).add(new Mountain());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void deathWithNoControllerLandsDealsNoDamage() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));
        harness.setGraveyard(player2, List.of(new Forest(), new Mountain()));
        cavalier.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }
}
