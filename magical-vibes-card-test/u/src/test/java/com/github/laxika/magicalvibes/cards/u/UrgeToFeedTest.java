package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrgeToFeed.class, VampireNighthawk.class, GrizzlyBears.class, AirElemental.class,
        Forest.class, IntoTheRoil.class})
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

    @Test
    void tapsAndCountersMultipleVampiresButExcludesTappedAndOpposingOnes() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        tapped.tap();
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new VampireNighthawk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castUrgeToFeed(target);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tapped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void killsTargetWhenThereAreNoEligibleVampires() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castUrgeToFeed(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    void decliningToTapZeroToughnessVampireLetsItDie() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());

        castUrgeToFeed(vampire);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Vampire Nighthawk");
        harness.assertInGraveyard(player1, "Vampire Nighthawk");
    }

    @Test
    void illegalTargetPreventsTappingAndCounters() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new UrgeToFeed()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInHand(player2, "Air Elemental");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(vampire.isTapped()).isFalse();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Urge to Feed");
    }

    @Test
    void shrinkExpiresAtEndOfTurnButCounterRemains() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());

        castUrgeToFeed(vampire);
        harness.handleMultiplePermanentsChosen(player1, List.of(vampire.getId()));
        assertThat(vampire.getEffectivePower()).isZero();
        assertThat(vampire.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vampire.getEffectivePower()).isEqualTo(3);
        assertThat(vampire.getEffectiveToughness()).isEqualTo(4);
    }

    private void castUrgeToFeed(Permanent target) {
        harness.setHand(player1, List.of(new UrgeToFeed()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
