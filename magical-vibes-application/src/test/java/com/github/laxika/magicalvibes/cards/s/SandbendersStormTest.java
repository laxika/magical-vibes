package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandbendersStorm.class, Forest.class, SerpentOfThePass.class})
class SandbendersStormTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a creature with power 4 or greater")
    void destroysLargeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerpentOfThePass());

        cast(0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Serpent of the Pass");
        harness.assertInGraveyard(player2, "Serpent of the Pass");
    }

    @Test
    @DisplayName("Earthbend animates a land and puts three counters on it")
    void earthbendsLand() {
        Permanent land = addForest(player1);

        cast(1, land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("An earthbended land returns tapped after it dies")
    void returnsTappedFromGraveyardAfterDeath() {
        Permanent land = addForest(player1);
        cast(1, land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(land.getCard().getId()));
        assertThat(gqs.isLand(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("An earthbended land returns tapped after it is exiled")
    void returnsTappedFromExileAfterLeaving() {
        Permanent land = addForest(player1);
        cast(1, land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Each mode enforces its own target restriction")
    void modesRejectIllegalTargets() {
        Permanent land = addForest(player2);
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("The destroy mode accepts an animated land with exactly four power")
    void destroysCreatureWithExactlyFourPower() {
        Permanent land = addForest(player1);
        cast(1, land.getId());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        cast(0, land.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The destroy mode rejects a creature with three power")
    void rejectsCreatureBelowPowerThreshold() {
        Permanent land = addForest(player1);
        cast(1, land.getId());
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The destroy mode rejects a noncreature land")
    void rejectsNoncreature() {
        Permanent land = addForest(player1);
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destroy mode rechecks power when resolving")
    void doesNotDestroyTargetWhosePowerDropsBelowFour() {
        Permanent land = addForest(player1);
        cast(1, land.getId());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{0}, List.of(land.getId()));

        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").getId()).isEqualTo(land.getId());
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Sandbenders' Storm");
    }

    @Test
    @DisplayName("Earthbending an animated land again adds counters and returns it only once")
    void repeatedEarthbendAddsCountersAndReturnsFreshLand() {
        Permanent land = addForest(player1);
        cast(1, land.getId());
        cast(1, land.getId());

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    private Permanent addForest(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SandbendersStorm()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{mode}, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
