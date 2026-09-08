package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrgeToFeedTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -3/-3 and puts a counter on each selected Vampire")
    void shrinksTargetAndCountersSelectedVampires() {
        Permanent targetVampire = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent otherVampire = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent nonVampire = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castUrgeToFeed(targetVampire);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(targetVampire.getId(), otherVampire.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(targetVampire.getId()));

        assertThat(targetVampire.isTapped()).isTrue();
        assertThat(targetVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(targetVampire.getEffectivePower()).isEqualTo(0);
        assertThat(targetVampire.getEffectiveToughness()).isEqualTo(1);
        assertThat(otherVampire.isTapped()).isFalse();
        assertThat(otherVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonVampire.isTapped()).isFalse();
        assertThat(nonVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Choosing no Vampires leaves them untapped and applies only the shrink")
    void choosingNoVampiresDoesNothingBeyondShrink() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castUrgeToFeed(target);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(vampire.isTapped()).isFalse();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new UrgeToFeed()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castUrgeToFeed(Permanent target) {
        harness.setHand(player1, List.of(new UrgeToFeed()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
