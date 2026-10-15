package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Fatestitcher;
import com.github.laxika.magicalvibes.cards.g.GoldPan;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NurturingPixie.class, SterlingHound.class, Island.class, GoldPan.class, Fatestitcher.class})
class NurturingPixieTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a non-Faerie nonland permanent and gets a +1/+1 counter")
    void returnsEligiblePermanentAndGetsCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Sterling Hound");
        Permanent pixie = findPermanent(player1, "Nurturing Pixie");
        assertThat(pixie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can decline the optional target")
    void canDeclineTarget() {
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pixie = findPermanent(player1, "Nurturing Pixie");
        assertThat(pixie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a land or a Faerie")
    void rejectsLandAndFaerieTargets() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Faerie, nonland permanent you control");

        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new NurturingPixie());
        assertThatThrownBy(() -> harness.castCreature(player1, 0, faerie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Faerie, nonland permanent you control");
    }

    @Test
    @DisplayName("Can choose no target even when an eligible permanent exists")
    void canDeclineWithEligiblePermanent() {
        harness.addToBattlefield(player1, new SterlingHound());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sterling Hound");
        assertThat(findPermanent(player1, "Nurturing Pixie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void rejectsOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Faerie, nonland permanent you control");
    }

    @Test
    @DisplayName("Can return a noncreature artifact and get a counter")
    void returnsNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldPan());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gold Pan");
        harness.assertInHand(player1, "Gold Pan");
        assertThat(findPermanent(player1, "Nurturing Pixie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets no counter when the target leaves before the trigger resolves")
    void noCounterWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sterling Hound");
        harness.assertNotInHand(player1, "Sterling Hound");
        assertThat(findPermanent(player1, "Nurturing Pixie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({NurturingPixie.class, Fatestitcher.class})
    @DisplayName("Gets no counter when unearth replaces the return with exile")
    void noCounterWhenReturnIsReplacedByExile() {
        Fatestitcher targetCard = new Fatestitcher();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        Permanent target = findPermanent(player1, "Fatestitcher");

        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fatestitcher");
        harness.assertNotInHand(player1, "Fatestitcher");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(targetCard);
        assertThat(findPermanent(player1, "Nurturing Pixie")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Still returns the target when Nurturing Pixie leaves before resolution")
    void returnsTargetAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        harness.setHand(player1, List.of(new NurturingPixie()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent pixie = findPermanent(player1, "Nurturing Pixie");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pixie));
        resolveAllTriggers();

        harness.assertInHand(player1, "Sterling Hound");
        harness.assertNotOnBattlefield(player1, "Sterling Hound");
        harness.assertInGraveyard(player1, "Nurturing Pixie");
        assertThat(pixie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
