package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second)));

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
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bolt);
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

    private Permanent addExpedition() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new SoulStairExpedition());
        expedition.setSummoningSick(false);
        return expedition;
    }
}
