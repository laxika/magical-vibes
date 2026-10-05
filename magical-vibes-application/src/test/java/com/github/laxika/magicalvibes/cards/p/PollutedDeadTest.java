package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PollutedDead.class, Forest.class, GrizzlyBears.class, Shock.class})
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

    @Test
    @DisplayName("Death trigger must destroy your own land when it is the only land")
    void destroysControllersOnlyLand() {
        harness.addToBattlefield(player1, new PollutedDead());
        harness.addToBattlefield(player1, new Forest());
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID deadId = harness.getPermanentId(player1, "Polluted Dead");
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forestId);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Polluted Dead");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Dying with no lands does not request a target or destroy another permanent")
    void diesWithoutAnyLands() {
        harness.addToBattlefield(player1, new PollutedDead());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID deadId = harness.getPermanentId(player1, "Polluted Dead");
        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, deadId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Polluted Dead");
        harness.assertInGraveyard(player1, "Polluted Dead");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonlethal damage does not trigger land destruction")
    void nonlethalDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new PollutedDead());
        harness.addToBattlefield(player2, new Forest());
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Polluted Dead"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Polluted Dead");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
