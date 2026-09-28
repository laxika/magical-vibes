package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AgentsToolkit.class, GrizzlyBears.class})
class AgentsToolkitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with +1/+1, flying, deathtouch, and shield counters")
    void entersWithCounters() {
        Permanent toolkit = addToolkit();

        assertThat(toolkit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(toolkit.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(toolkit.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(toolkit.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("May move a chosen counter onto a creature you control that enters")
    void movesChosenCounterOntoEnteringCreature() {
        Permanent toolkit = addToolkit();
        castBears(player1);
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "flying counters");
        harness.passBothPriorities();

        assertThat(toolkit.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(bears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(toolkit.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing it for {2} draws a card")
    void sacrificesAndDraws() {
        Permanent toolkit = harness.addToBattlefieldAndReturn(player1, new AgentsToolkit());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(toolkit);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    private Permanent addToolkit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AgentsToolkit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Agent's Toolkit");
    }

    private void castBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }
}
