package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PollutedDeadTest extends BaseCardTest {

    @Test
    @DisplayName("When Polluted Dead dies, destroy target land")
    void diesDestroysTargetLand() {
        harness.addToBattlefield(player1, new PollutedDead());
        harness.addToBattlefield(player2, new Forest());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID deadId = harness.getPermanentId(player1, "Polluted Dead");
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities(); // 3/3 takes 4 damage → dies → death trigger awaits target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Death trigger only offers lands as valid targets")
    void targetFilterOnlyLands() {
        harness.addToBattlefield(player1, new PollutedDead());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID deadId = harness.getPermanentId(player1, "Polluted Dead");
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forestId);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
