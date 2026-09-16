package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AGoodThing.class, Disenchant.class})
class AGoodThingTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, your life total is doubled")
    void doublesControllerLifeAtUpkeep() {
        harness.addToBattlefield(player1, new AGoodThing());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("The controller loses after doubling to at least 1,000 life")
    void losesAtLifeThreshold() {
        harness.addToBattlefield(player1, new AGoodThing());
        harness.setLife(player1, 600);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AGoodThing());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The controller's spells cannot target A Good Thing")
    void ownSpellCannotTargetIt() {
        var goodThing = harness.addToBattlefieldAndReturn(player1, new AGoodThing());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, goodThing.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be targeted");
    }

    @Test
    @DisplayName("An opponent's spell can target A Good Thing")
    void opponentSpellCanTargetIt() {
        var goodThing = harness.addToBattlefieldAndReturn(player1, new AGoodThing());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, goodThing.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "A Good Thing");
    }
}
