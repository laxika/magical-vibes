package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InsideOutTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inside Out switches power and toughness and draws a card")
    void switchesPowerToughnessAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setPowerModifier(1); // Grizzly Bears 2/2 -> 3/2
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new InsideOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // 3/2 switches to 2/3
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Switch wears off at cleanup step")
    void switchWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setPowerModifier(1); // 3/2

        harness.setHand(player1, List.of(new InsideOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Inside Out fizzles and does not draw when target is removed")
    void fizzlesAndDoesNotDrawWhenTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new InsideOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Inside Out")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // legal creature target so the spell is castable (CR 601.2c)
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new InsideOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
