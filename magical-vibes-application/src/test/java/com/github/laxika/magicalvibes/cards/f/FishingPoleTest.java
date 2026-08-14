package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FishingPoleTest extends BaseCardTest {

    @Test
    @DisplayName("The granted ability taps both permanents and puts a bait counter on Fishing Pole")
    void baitAbilityTapsCreatureAndPole() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pole = addPoleReady(player1);
        pole.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(pole.isTapped()).isTrue();
        assertThat(pole.getCounterCount(CounterType.BAIT)).isEqualTo(1);
    }

    @Test
    @DisplayName("The untap trigger removes a bait counter and creates a Fish")
    void untappingEquippedCreatureCreatesFish() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pole = addPoleReady(player1);
        pole.setAttachedTo(creature.getId());
        pole.setCounterCount(CounterType.BAIT, 1);
        creature.tap();

        runUntapStep(player1);
        harness.passBothPriorities();

        assertThat(pole.getCounterCount(CounterType.BAIT)).isZero();
        assertThat(countPermanents(player1, "Fish")).isEqualTo(1);
    }

    @Test
    @DisplayName("The untap trigger resolves without a Fish when Fishing Pole has no bait")
    void untappingWithNoBaitCreatesNoFish() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pole = addPoleReady(player1);
        pole.setAttachedTo(creature.getId());
        creature.tap();

        runUntapStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Fish")).isZero();
    }

    @Test
    @DisplayName("A Pole controlled by another player still triggers for its equipped creature")
    void opponentControlledPoleTriggersForEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pole = addPoleReady(player2);
        pole.setAttachedTo(creature.getId());
        pole.setCounterCount(CounterType.BAIT, 1);
        creature.tap();

        runUntapStep(player1);
        harness.passBothPriorities();

        assertThat(pole.getCounterCount(CounterType.BAIT)).isZero();
        assertThat(countPermanents(player2, "Fish")).isEqualTo(1);
        assertThat(countPermanents(player1, "Fish")).isZero();
    }

    @Test
    @DisplayName("The granted ability cannot be activated while Fishing Pole is tapped")
    void baitAbilityRequiresUntappedPole() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pole = addPoleReady(player1);
        pole.setAttachedTo(creature.getId());
        pole.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("granting Equipment is already tapped");
    }

    private Permanent addPoleReady(Player player) {
        Permanent perm = new Permanent(new FishingPole());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
