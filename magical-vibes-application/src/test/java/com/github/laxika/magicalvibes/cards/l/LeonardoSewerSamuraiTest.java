package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardoSewerSamurai.class, GoblinPiker.class, GrizzlyBears.class, Ornithopter.class})
class LeonardoSewerSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature with power 1 or less from the graveyard with a finality counter")
    void castsCreatureWithLowPowerFromGraveyard() {
        Card creature = new Ornithopter();
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanentByCardId(creature.getId());
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("casts a creature with toughness 1 or less from the graveyard with a finality counter")
    void castsCreatureWithLowToughnessFromGraveyard() {
        Card creature = new GoblinPiker();
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhaseFor(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanentByCardId(creature.getId());
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("does not cast a creature with power and toughness greater than 1")
    void rejectsCreatureWithHighPowerAndToughness() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhaseFor(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("does not grant graveyard casting permission during an opponent's turn")
    void permissionIsLimitedToControllerTurn() {
        harness.addToBattlefield(player1, new LeonardoSewerSamurai());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of());
        prepareMainPhaseFor(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhaseFor(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
