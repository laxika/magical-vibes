package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromiseOfBunreiTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and creates four colorless Spirit tokens when your creature dies")
    void creatureDeathCreatesFourSpirits() {
        harness.addToBattlefield(player1, new PromiseOfBunrei());
        harness.addToBattlefield(player1, new GrizzlyBears());
        killPlayerOnesCreature();

        harness.assertInGraveyard(player1, "Promise of Bunrei");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(4)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getPower()).isEqualTo(1);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(1);
                    assertThat(permanent.getCard().getColor()).isNull();
                    assertThat(permanent.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
                });
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new PromiseOfBunrei());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Promise of Bunrei");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .isEmpty();
    }

    private void killPlayerOnesCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
