package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VolcanicHammer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireSnake.class, Forest.class, GrizzlyBears.class, VolcanicHammer.class})
class FireSnakeTest extends BaseCardTest {

    @Test
    @DisplayName("When Fire Snake dies, destroy target land")
    void diesDestroysTargetLand() {
        Permanent snake = harness.addToBattlefieldAndReturn(player1, new FireSnake());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new VolcanicHammer()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0, snake.getId());
        harness.passBothPriorities(); // Volcanic Hammer resolves → snake dies → death trigger awaits target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Death trigger only offers lands as valid targets")
    void targetFilterOnlyLands() {
        Permanent snake = harness.addToBattlefieldAndReturn(player1, new FireSnake());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new VolcanicHammer()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0, snake.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
    }

    @Test
    @DisplayName("Death trigger can destroy a land controlled by Fire Snake's controller")
    void canDestroyOwnLand() {
        Permanent snake = harness.addToBattlefieldAndReturn(player1, new FireSnake());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new VolcanicHammer()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0, snake.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
