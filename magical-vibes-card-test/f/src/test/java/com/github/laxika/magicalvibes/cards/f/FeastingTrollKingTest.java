package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FeastingTrollKing.class})
@DisplayName("Feasting Troll King")
class FeastingTrollKingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three Food tokens when cast from hand")
    void createsThreeFoodTokensWhenCastFromHand() {
        castTrollFromHand();

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifices three Foods to return from the graveyard")
    void sacrificesThreeFoodsToReturnFromGraveyard() {
        Permanent troll = castTrollFromHand();
        moveTrollToGraveyard(troll);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertOnBattlefield(player1, "Feasting Troll King");
        harness.assertNotInGraveyard(player1, "Feasting Troll King");
    }

    @Test
    @DisplayName("Cannot activate without three Foods")
    void cannotActivateWithoutThreeFoods() {
        Permanent troll = castTrollFromHand();
        moveTrollToGraveyard(troll);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Food"));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during your turn")
    void canActivateOnlyDuringYourTurn() {
        Permanent troll = castTrollFromHand();
        moveTrollToGraveyard(troll);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering without being cast does not create Food")
    void enteringWithoutCastingDoesNotCreateFood() {
        harness.enterBattlefieldAndReturn(player1, new FeastingTrollKing());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertOnBattlefield(player1, "Feasting Troll King");
    }

    @Test
    @DisplayName("Food tokens can be sacrificed for three life")
    void foodTokenGainsThreeLife() {
        castTrollFromHand();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food"));

        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can return during your upkeep without creating more Food")
    void returnsDuringUpkeepWithoutCreatingFood() {
        Permanent troll = castTrollFromHand();
        moveTrollToGraveyard(troll);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertInGraveyard(player1, "Feasting Troll King");
        harness.assertNotOnBattlefield(player1, "Feasting Troll King");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Feasting Troll King");
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns only the copy whose ability was activated")
    void returnsOnlyActivatedCopy() {
        Permanent troll = castTrollFromHand();
        moveTrollToGraveyard(troll);
        FeastingTrollKing otherTroll = new FeastingTrollKing();
        harness.setGraveyard(player1, List.of(troll.getCard(), otherTroll));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherTroll);
        assertThat(findPermanent(player1, "Feasting Troll King").getCard().getId())
                .isEqualTo(troll.getCard().getId());
        assertThat(countPermanents(player1, "Feasting Troll King")).isEqualTo(1);
    }

    private Permanent castTrollFromHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FeastingTrollKing()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Feasting Troll King");
    }

    private void moveTrollToGraveyard(Permanent troll) {
        gd.playerBattlefields.get(player1.getId()).remove(troll);
        harness.setGraveyard(player1, List.of(troll.getCard()));
    }
}
