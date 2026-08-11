package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinbindingTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures by the number of creatures that entered this turn")
    void boostsByCreaturesEnteredThisTurn() {
        harness.addToBattlefield(player1, new Kinbinding());
        Permanent existingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a green and white Kithkin token at the beginning of your combat")
    void createsKithkinTokenAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new Kinbinding());

        advanceToCombat(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Kithkin");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(
                CardColor.GREEN, CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
