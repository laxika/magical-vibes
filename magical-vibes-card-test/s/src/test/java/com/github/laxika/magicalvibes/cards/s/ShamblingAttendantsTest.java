package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WoollyLoxodon;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ShamblingAttendants.class, WoollyLoxodon.class})
class ShamblingAttendantsTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(),
                new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5, 6));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        harness.assertOnBattlefield(player1, "Shambling Attendants");
    }

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new ShamblingAttendants());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoollyLoxodon());

        attendant.setSummoningSick(false);
        attendant.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attendant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Delve is optional when the full mana cost is paid")
    void canPayFullManaCostWithoutDelving() {
        Card graveyardCard = new WoollyLoxodon();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shambling Attendants");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Partial delve exiles only the selected cards and pays the rest with mana")
    void canCombineDelveWithMana() {
        Card first = new WoollyLoxodon();
        Card unselected = new WoollyLoxodon();
        Card last = new WoollyLoxodon();
        harness.setGraveyard(player1, List.of(first, unselected, last));
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shambling Attendants");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, last);
    }

    @Test
    @DisplayName("Delve cannot replace the required black mana")
    void delveDoesNotPayColoredMana() {
        List<Card> graveyard = List.of(
                new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(),
                new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5, 6)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shambling Attendants");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Delve cannot exile more cards than the generic cost")
    void cannotDelveMoreThanGenericCost() {
        List<Card> graveyard = List.of(
                new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(),
                new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon(), new WoollyLoxodon());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5, 6, 7)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shambling Attendants");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A graveyard card cannot be counted twice for delve")
    void cannotDelveTheSameCardTwice() {
        Card graveyardCard = new WoollyLoxodon();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new ShamblingAttendants()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shambling Attendants");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
