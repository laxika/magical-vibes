package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GameOver.class, FountainOfYouth.class, GrizzlyBears.class})
class GameOverTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreaturesAndLeavesNoncreaturesAlone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());

        castGameOver("{3}{B}{B}");

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Costs two less when any player has half their starting life or less")
    void costsTwoLessWhenAnyPlayerHasHalfStartingLife() {
        harness.setLife(player2, 10);

        harness.setHand(player1, List.of(new GameOver()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
    }

    @Test
    @DisplayName("Does not get the cost reduction when every player is above half their starting life")
    void doesNotGetCostReductionWhenEveryPlayerIsAboveHalfStartingLife() {
        harness.setHand(player1, List.of(new GameOver()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with a regeneration shield survives")
    void regenerationShieldSavesCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        castGameOver("{3}{B}{B}");

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castGameOver(String manaCost) {
        harness.castFromHand(player1, new GameOver(), manaCost);
        harness.passBothPriorities();
    }
}
