package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
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

@CardUsed({WaylayingPirates.class, GrizzlyBears.class, Millstone.class})
class WaylayingPiratesTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an opponent's creature and puts a stun counter on it when you control an artifact")
    void tapsAndStunsOpponentCreatureWithControlledArtifact() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWaylayingPirates(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's noncreature artifact")
    void tapsAndStunsOpponentArtifact() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castWaylayingPirates(artifact);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when you do not control an artifact")
    void doesNothingWithoutControlledArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WaylayingPirates()));
        addManaForWaylayingPirates();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaylayingPirates()));
        addManaForWaylayingPirates();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.isTapped()).isFalse();
    }

    private void castWaylayingPirates(Permanent target) {
        harness.setHand(player1, List.of(new WaylayingPirates()));
        addManaForWaylayingPirates();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void addManaForWaylayingPirates() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
