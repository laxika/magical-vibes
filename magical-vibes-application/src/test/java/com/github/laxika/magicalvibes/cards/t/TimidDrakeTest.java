package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimidDrake.class, BenalishKnight.class, MindStone.class})
class TimidDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to hand when another creature the controller casts enters")
    void bouncesSelfOnAllyCreatureEntering() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.setHand(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof TimidDrake);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof TimidDrake);
    }

    @Test
    @DisplayName("Returns itself to hand when an opponent's creature enters")
    void bouncesSelfOnOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BenalishKnight()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castCreature(player2, 0);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof TimidDrake);
    }

    @Test
    @DisplayName("Does not trigger on its own entry")
    void doesNotTriggerOnSelfEntering() {
        harness.setHand(player1, List.of(new TimidDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof TimidDrake);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature artifact enters")
    void doesNotTriggerOnNonCreatureEntering() {
        harness.addToBattlefield(player1, new TimidDrake());

        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof TimidDrake);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c instanceof TimidDrake);
    }
}
