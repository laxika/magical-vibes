package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EternalStudent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Homesickness.class, EternalStudent.class})
class HomesicknessTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot choose more than two creature targets")
    void cannotTargetThreeCreatures() {
        Permanent first = addCreatureReady(player2, new EternalStudent());
        Permanent second = addCreatureReady(player2, new EternalStudent());
        Permanent third = addCreatureReady(player2, new EternalStudent());
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player2.getId(), first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target player draws two; both creatures are tapped and stunned")
    void drawsAndStunsTwoCreatures() {
        Permanent bear = addCreatureReady(player2, new EternalStudent());
        Permanent spider = addCreatureReady(player2, new EternalStudent());

        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), bear.getId(), spider.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves drawing cards even when no creatures are targeted")
    void resolvesWithNoCreatureTargets() {
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Homesickness");
    }

    @Test
    @DisplayName("Cannot target a creature as the player target")
    void cannotTargetCreatureAsPlayer() {
        Permanent bear = addCreatureReady(player2, new EternalStudent());
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID bearId = bear.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can draw for yourself and stun an already tapped creature you control")
    void drawsForControllerAndStunsTappedCreature() {
        Permanent creature = addCreatureReady(player1, new EternalStudent());
        creature.tap();
        creature.setCounterCount(CounterType.STUN, 1);
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Homesickness");
    }

    @Test
    @DisplayName("A creature leaving does not prevent drawing or stunning the remaining target")
    void resolvesForRemainingLegalTargets() {
        Permanent removed = addCreatureReady(player2, new EternalStudent());
        Permanent remaining = addCreatureReady(player1, new EternalStudent());
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, List.of(player2.getId(), removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        gd.playerGraveyards.get(player2.getId()).add(removed.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(remaining.isTapped()).isTrue();
        assertThat(remaining.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.STUN)).isZero();
        harness.assertInGraveyard(player1, "Homesickness");
    }

    @Test
    @DisplayName("Still draws when the only creature target leaves before resolution")
    void drawsWhenAllCreatureTargetsLeave() {
        Permanent removed = addCreatureReady(player2, new EternalStudent());
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.castInstant(player1, 0, List.of(player2.getId(), removed.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        gd.playerGraveyards.get(player2.getId()).add(removed.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(removed.getCounterCount(CounterType.STUN)).isZero();
        harness.assertInGraveyard(player1, "Homesickness");
    }

    @Test
    @DisplayName("Cannot use a player as an optional creature target")
    void cannotTargetPlayerAsCreature() {
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
