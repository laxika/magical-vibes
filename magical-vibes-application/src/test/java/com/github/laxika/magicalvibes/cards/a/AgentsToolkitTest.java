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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    @ParameterizedTest
    @EnumSource(value = CounterType.class, names = {"PLUS_ONE_PLUS_ONE", "FLYING", "DEATHTOUCH", "SHIELD"})
    void movesExactlyOneOfTheOnlyRemainingCounterType(CounterType counterType) {
        Permanent toolkit = addToolkit();
        for (CounterType type : CounterType.values()) {
            toolkit.setCounterCount(type, type == counterType ? 2 : 0);
        }
        castBears(player1);
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(toolkit.getCounterCount(counterType)).isEqualTo(1);
        assertThat(bears.getCounterCount(counterType)).isEqualTo(1);
    }

    @Test
    void mayDeclineMovingACounter() {
        Permanent toolkit = addToolkit();
        castBears(player1);
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        for (CounterType type : List.of(CounterType.PLUS_ONE_PLUS_ONE, CounterType.FLYING,
                CounterType.DEATHTOUCH, CounterType.SHIELD)) {
            assertThat(toolkit.getCounterCount(type)).isEqualTo(1);
            assertThat(bears.getCounterCount(type)).isZero();
        }
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        Permanent toolkit = addToolkit();
        castBears(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(toolkit.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void canMoveACounterOtherThanTheFourInitialKinds() {
        Permanent toolkit = addToolkit();
        for (CounterType type : CounterType.values()) {
            toolkit.setCounterCount(type, type == CounterType.HASTE ? 1 : 0);
        }
        castBears(player1);
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(toolkit.getCounterCount(CounterType.HASTE)).isZero();
        assertThat(bears.getCounterCount(CounterType.HASTE)).isEqualTo(1);
    }

    @Test
    void offersAdditionalCounterKindsAlongsideInitialCounters() {
        Permanent toolkit = addToolkit();
        toolkit.setCounterCount(CounterType.HASTE, 1);
        castBears(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("haste counters");
        harness.handleListChoice(player1, "haste counters");
        harness.passBothPriorities();

        assertThat(toolkit.getCounterCount(CounterType.HASTE)).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.HASTE)).isEqualTo(1);
    }

    @Test
    void triggersWithoutCountersAndCanMoveACounterAddedBeforeResolution() {
        Permanent toolkit = addToolkit();
        for (CounterType type : CounterType.values()) {
            toolkit.setCounterCount(type, 0);
        }
        castBears(player1);
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        toolkit.setCounterCount(CounterType.FLYING, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(toolkit.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(bears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    void shieldCounterDoesNotPreventSacrificingToolkit() {
        Permanent toolkit = addToolkit();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(toolkit);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof AgentsToolkit);
    }

    private Permanent addToolkit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AgentsToolkit(), "{1}{G}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Agent's Toolkit");
    }

    private void castBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
    }
}
