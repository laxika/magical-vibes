package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({LeosGuidance.class, GrizzlyBears.class, Mountain.class})
class LeosGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on and untaps each of up to three target creatures")
    void putsCounterOnAndUntapsEachTargetCreature() {
        Permanent first = addTappedCreature(player1);
        Permanent second = addTappedCreature(player1);
        Permanent third = addTappedCreature(player2);

        cast(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target only one creature")
    void canTargetOnlyOneCreature() {
        Permanent creature = addTappedCreature(player1);

        cast(List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new LeosGuidance()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can resolve with no targets without affecting other creatures")
    void canResolveWithNoTargets() {
        Permanent creature = addTappedCreature(player1);

        cast(List.of());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Leo's Guidance");
    }

    @Test
    @DisplayName("Puts a counter on an already untapped creature")
    void putsCounterOnUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target more than three creatures")
    void cannotTargetFourCreatures() {
        Permanent first = addTappedCreature(player1);
        Permanent second = addTappedCreature(player1);
        Permanent third = addTappedCreature(player2);
        Permanent fourth = addTappedCreature(player2);
        harness.setHand(player1, List.of(new LeosGuidance()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolves for the remaining creature when another target leaves the battlefield")
    void resolvesForRemainingLegalTarget() {
        Permanent departed = addTappedCreature(player1);
        Permanent remaining = addTappedCreature(player2);
        harness.setHand(player1, List.of(new LeosGuidance()));
        addMana();
        harness.castInstant(player1, 0, List.of(departed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(departed);
        gd.playerGraveyards.get(player1.getId()).add(departed.getCard());

        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(remaining.isTapped()).isFalse();
        assertThat(departed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(departed.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Leo's Guidance");
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.tap();
        return creature;
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new LeosGuidance()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 3);
    }
}
