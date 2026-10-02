package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppleOfEdenIsuRelic.class, Forest.class, GrizzlyBears.class, Island.class})
class AppleOfEdenIsuRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target opponent's hand face down and lets its controller cast a card")
    void exilesHandAndCastsSpellWithOwnerDrawTrigger() {
        Permanent apple = addApple();
        GrizzlyBears spell = new GrizzlyBears();
        Forest land = new Forest();
        Island drawn = new Island();
        harness.setHand(player2, List.of(spell, land));
        harness.setLibrary(player2, List.of(drawn));

        activateApple(apple);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apple);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .allMatch(entry -> entry.faceDown() && apple.getId().equals(entry.sourcePermanentId()))
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(spell, land);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(land);
    }

    @Test
    @DisplayName("Playing an exiled land also makes its owner draw a card")
    void playingExiledLandTriggersOwnerDraw() {
        Permanent apple = addApple();
        Forest land = new Forest();
        Island drawn = new Island();
        harness.setHand(player2, List.of(land));
        harness.setLibrary(player2, List.of(drawn));

        activateApple(apple);
        harness.castFromExile(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    @DisplayName("Returns cards that were not played at the next end step")
    void returnsUnplayedCardsAtNextEndStep() {
        Permanent apple = addApple();
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setHand(player2, List.of(exiled));

        activateApple(apple);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(exiled);
    }

    @Test
    @DisplayName("All unplayed cards return through one delayed triggered ability")
    void returnsAllCardsWithOneEndStepTrigger() {
        Permanent apple = addApple();
        GrizzlyBears spell = new GrizzlyBears();
        Forest land = new Forest();
        harness.setHand(player2, List.of(spell, land));

        activateApple(apple);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(spell, land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent paying the activation costs")
    void canActivateAgainstEmptyHand() {
        Permanent apple = addApple();
        harness.setHand(player2, List.of());

        activateApple(apple);

        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apple);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        Permanent apple = addApple();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apple), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apple);
    }

    @Test
    @DisplayName("The ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        Permanent apple = addApple();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apple), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apple);
    }

    @Test
    @DisplayName("Activation requires four life")
    void cannotActivateWithoutEnoughLife() {
        Permanent apple = addApple();
        harness.setLife(player1, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apple), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apple);
    }

    @Test
    @DisplayName("A tapped Apple cannot pay its tap cost")
    void cannotActivateWhenTapped() {
        Permanent apple = addApple();
        apple.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apple), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apple);
    }

    @Test
    @DisplayName("Playing an exiled land still consumes the normal land play")
    void cannotPlaySecondExiledLand() {
        Permanent apple = addApple();
        Forest first = new Forest();
        Island second = new Island();
        harness.setHand(player2, List.of(first, second));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        activateApple(apple);
        harness.castFromExile(player1, first.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Casting from hand does not cause Apple's delayed draw trigger")
    void castingFromHandDoesNotMakeOpponentDraw() {
        Permanent apple = addApple();
        GrizzlyBears ownSpell = new GrizzlyBears();
        Forest exiled = new Forest();
        Island topCard = new Island();
        harness.setHand(player1, List.of(ownSpell));
        harness.setHand(player2, List.of(exiled));
        harness.setLibrary(player2, List.of(topCard));

        activateApple(apple);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
    }

    private Permanent addApple() {
        return harness.addToBattlefieldAndReturn(player1, new AppleOfEdenIsuRelic());
    }

    private void activateApple(Permanent apple) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apple),
                null, player2.getId());
        harness.passBothPriorities();
    }
}
