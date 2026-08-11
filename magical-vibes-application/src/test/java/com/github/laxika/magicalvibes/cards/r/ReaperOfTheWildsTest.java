package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReaperOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature dying makes Reaper of the Wilds scry 1")
    void anotherCreatureDiesTriggersScry() {
        addReaper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reaper of the Wilds does not trigger when it dies")
    void ownDeathDoesNotTrigger() {
        addReaper(player1);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID reaperId = harness.getPermanentId(player1, "Reaper of the Wilds");
        harness.castInstant(player2, 0, reaperId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Black ability grants deathtouch until end of turn")
    void blackAbilityGrantsDeathtouchUntilEndOfTurn() {
        Permanent reaper = addReaper(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.DEATHTOUCH)).isTrue();

        forceEndStep();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Green ability grants hexproof until end of turn")
    void greenAbilityGrantsHexproofUntilEndOfTurn() {
        Permanent reaper = addReaper(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.HEXPROOF)).isTrue();

        forceEndStep();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addReaper(Player player) {
        return addCreatureReady(player, new ReaperOfTheWilds());
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void forceEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
