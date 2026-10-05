package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BelligerentRegisaur;
import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelligerentRegisaur.class, CopperHostCrusher.class, Forest.class,
        InvasionOfIxalan.class, Island.class, Plains.class, VolcanicSpite.class})
class InvasionOfIxalanTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege may reveal a permanent from the top five and bottoms the rest randomly")
    void looksAtTopFiveForPermanent() {
        Card creature = new CopperHostCrusher();
        Card instant = new VolcanicSpite();
        Card forest = new Forest();
        Card plains = new Plains();
        Card island = new Island();
        harness.setLibrary(player1, List.of(creature, instant, forest, plains, island));

        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).contains(creature.getId(), forest.getId(), plains.getId(), island.getId())
                .doesNotContain(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(instant, forest, plains, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the defeated Siege's cast casts Belligerent Regisaur transformed")
    void defeatCastsBackFace() {
        harness.setLibrary(player1, List.of());
        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanent(player1, "Invasion of Ixalan");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent regisaur = findPermanent(player1, "Belligerent Regisaur");
        assertThat(regisaur.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Belligerent Regisaur gains indestructible when you cast a spell")
    void spellCastGrantsIndestructible() {
        Permanent regisaur = harness.addToBattlefieldAndReturn(player1, new BelligerentRegisaur());
        harness.castFromHand(player1, new CopperHostCrusher(), "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void mayDeclinePermanentAndLeavesSixthCardOnTop() {
        Card forest = new Forest();
        Card plains = new Plains();
        Card island = new Island();
        Card battle = new InvasionOfIxalan();
        Card anotherForest = new Forest();
        Card sixth = new Plains();
        harness.setLibrary(player1, List.of(forest, plains, island, battle, anotherForest, sixth));

        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).contains(battle.getId()).doesNotContain(sixth.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(forest, plains, island, battle, anotherForest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canChooseLandFromShortLibrary() {
        Card forest = new Forest();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));

        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCardsAreAllReturnedWithoutAChoice() {
        Card first = new VolcanicSpite();
        Card second = new VolcanicSpite();
        harness.setLibrary(player1, List.of(first, second));

        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void defeatingSiegeAllowsControllerToDeclineCastingBackFace() {
        harness.setLibrary(player1, List.of());
        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent battle = findPermanent(player1, "Invasion of Ixalan");
        Card frontCard = battle.getCard();
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(frontCard.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.findExiledCard(frontCard.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Belligerent Regisaur");
    }

    @Test
    void noncreatureSpellTriggerResolvesBeforeSpellAndExpiresAtEndOfTurn() {
        Permanent regisaur = harness.addToBattlefieldAndReturn(player1, new BelligerentRegisaur());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castInvasion();

        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Invasion of Ixalan");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void opponentsSpellDoesNotGrantIndestructible() {
        Permanent regisaur = harness.addToBattlefieldAndReturn(player1, new BelligerentRegisaur());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new CopperHostCrusher(), "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void playingLandDoesNotGrantIndestructible() {
        Permanent regisaur = harness.addToBattlefieldAndReturn(player1, new BelligerentRegisaur());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(regisaur.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfIxalan(), "{1}{G}");
    }
}
