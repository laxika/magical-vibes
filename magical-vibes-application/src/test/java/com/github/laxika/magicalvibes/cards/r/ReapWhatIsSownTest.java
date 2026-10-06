package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BileBlight;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.cards.p.PheresBandTromper;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
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

@CardUsed({ReapWhatIsSown.class, NyxbornWolf.class, PheresBandTromper.class, SpringleafDrum.class, BileBlight.class})
class ReapWhatIsSownTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of three target creatures")
    void putsCounterOnEachTargetCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());

        prepareReapWhatIsSown();
        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target fewer than three creatures")
    void canTargetFewerThanThreeCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());

        prepareReapWhatIsSown();
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        prepareReapWhatIsSown();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can resolve with no targets even when a creature is available")
    void canChooseZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        prepareReapWhatIsSown();

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reap What Is Sown");
    }

    @Test
    @DisplayName("Can target two creatures without affecting an unchosen creature")
    void canChooseTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PheresBandTromper());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        prepareReapWhatIsSown();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot choose four creatures")
    void cannotChooseMoreThanThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        prepareReapWhatIsSown();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Reap What Is Sown");
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        prepareReapWhatIsSown();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Reap What Is Sown");
    }

    @Test
    @DisplayName("Still puts a counter on a surviving target when another target dies in response")
    void resolvesForRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent surviving = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        prepareReapWhatIsSown();
        harness.castInstant(player1, 0, List.of(removed.getId(), surviving.getId()));

        castBileBlightInResponse(removed);
        harness.assertNotOnBattlefield(player1, "Nyxborn Wolf");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(surviving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Reap What Is Sown");
    }

    @Test
    @DisplayName("Does not put counters on creatures when all chosen targets die in response")
    void allTargetsBecomeIllegal() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        prepareReapWhatIsSown();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        castBileBlightInResponse(first);
        harness.assertNotOnBattlefield(player1, "Nyxborn Wolf");
        harness.assertNotOnBattlefield(player2, "Nyxborn Wolf");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reap What Is Sown");
    }

    private void castBileBlightInResponse(Permanent target) {
        harness.setHand(player2, List.of(new BileBlight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void prepareReapWhatIsSown() {
        harness.setHand(player1, List.of(new ReapWhatIsSown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
