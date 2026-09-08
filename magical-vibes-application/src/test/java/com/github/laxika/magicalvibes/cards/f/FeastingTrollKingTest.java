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

    private Permanent castTrollFromHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FeastingTrollKing()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Feasting Troll King");
    }

    private void moveTrollToGraveyard(Permanent troll) {
        gd.playerBattlefields.get(player1.getId()).remove(troll);
        harness.setGraveyard(player1, List.of(troll.getCard()));
    }
}
