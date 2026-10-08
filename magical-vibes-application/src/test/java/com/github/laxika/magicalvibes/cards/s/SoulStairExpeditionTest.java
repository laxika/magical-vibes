package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OranRiefSurvivalist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulStairExpedition.class, Forest.class, OranRiefSurvivalist.class, Disfigure.class})
class SoulStairExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers a quest counter")
    void landfallOffersQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining landfall adds no quest counter")
    void decliningLandfallAddsNoCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removing three quest counters and sacrificing returns up to two creature cards")
    void returnsUpToTwoCreatureCards() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card first = new OranRiefSurvivalist();
        Card second = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(first, second));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expedition.getCard());
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .contains(first.getId(), second.getId());
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card noncreature = new Disfigure();
        harness.setGraveyard(player1, List.of(noncreature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability requires three quest counters")
    void requiresThreeQuestCounters() {
        addExpedition();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice with zero targets even when a creature is available")
    void canChooseZeroTargets() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card creature = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, expedition.getCard());
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, expedition.getCard());
    }

    @Test
    @DisplayName("Can return only one creature even when two are available")
    void canChooseOneTarget() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 4);
        Card chosen = new OranRiefSurvivalist();
        Card unchosen = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(chosen, unchosen));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(chosen.getId()));

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen, expedition.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card creature = new OranRiefSurvivalist();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Cannot target the same creature card twice")
    void cannotChooseDuplicateTarget() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card creature = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Cannot target more than two creature cards")
    void cannotChooseThreeTargets() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card first = new OranRiefSurvivalist();
        Card second = new OranRiefSurvivalist();
        Card third = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(first, second, third));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Returns the remaining target if the other leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card removed = new OranRiefSurvivalist();
        Card remaining = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(removed, remaining));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining, expedition.getCard()));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expedition.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTrigger() {
        Permanent expedition = addExpedition();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Two quest counters cannot pay the activation cost")
    void cannotActivateWithOnlyTwoQuestCounters() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 2);
        Card creature = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(expedition);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No creature returns if every chosen target leaves the graveyard")
    void returnsNothingWhenAllTargetsLeave() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Card creature = new OranRiefSurvivalist();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of(expedition.getCard()));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expedition.getCard());
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addExpedition() {
        return harness.addToBattlefieldAndReturn(player1, new SoulStairExpedition());
    }
}
