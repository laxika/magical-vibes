package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExoticPets.class, GrizzlyBears.class})
class ExoticPetsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two unblockable Fish and puts each controlled counter kind on either one")
    void createsFishAndCopiesControlledCounterKinds() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondCreature.setCounterCount(CounterType.CHARGE, 1);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(fish).hasSize(2);
        assertThat(fish).allSatisfy(token -> assertThat(gqs.hasCantBeBlocked(gd, token)).isTrue());

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrderElementsOf(
                fish.stream().map(Permanent::getId).toList());

        harness.handlePermanentChosen(player1, fish.getFirst().getId());
        harness.handlePermanentChosen(player1, fish.getFirst().getId());

        assertThat(fish.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                + fish.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.CHARGE)
                + fish.get(1).getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(fish.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fish.get(1).getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Copies no counter kinds from creatures controlled by an opponent")
    void ignoresOpponentsCounters() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.CHARGE, 1);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(fish).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(fish).allSatisfy(token -> {
            assertThat(token.getCounterCount(CounterType.CHARGE)).isZero();
            assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        });
    }

    private void cast() {
        harness.setHand(player1, List.of(new ExoticPets()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
