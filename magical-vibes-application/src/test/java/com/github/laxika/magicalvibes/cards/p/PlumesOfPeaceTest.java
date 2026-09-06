package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PlumesOfPeace.class, GrizzlyBears.class, Forest.class})
class PlumesOfPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        creature.tap();

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forecast taps a creature and keeps Plumes of Peace in hand")
    void forecastTapsTargetAndKeepsSourceInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PlumesOfPeace plumes = new PlumesOfPeace();
        harness.setHand(player1, List.of(plumes));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plumes);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastIsLimitedToOncePerTurn() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlumesOfPeace()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, firstTarget.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires its controller's upkeep and a creature target")
    void forecastRequiresUpkeepAndCreatureTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        PlumesOfPeace plumes = new PlumesOfPeace();
        harness.setHand(player1, List.of(plumes));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plumes);
    }

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
