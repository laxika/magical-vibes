package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VATS.class, GrizzlyBears.class, HillGiant.class, Shock.class, LlanowarElves.class, RoyalAssassin.class})
class VATSTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any number of target creatures with equal toughness")
    void destroysEqualToughnessTargets() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        prepareCard();
        harness.castAndResolveInstant(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hillGiant);
    }

    @Test
    @DisplayName("Rejects targets with different toughness")
    void rejectsDifferentToughnessTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), hillGiant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("equal toughness");
    }

    @Test
    @DisplayName("Split second prevents casting spells in response")
    void splitSecondPreventsResponses() {
        prepareCard();
        harness.castInstant(player1, 0, List.of());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Split second prevents nonmana activated abilities")
    void splitSecondPreventsNonmanaAbilities() {
        Permanent assassin = addCreatureReady(player2, new RoyalAssassin());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();
        cast(List.of(bear.getId()));
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");
        assertThat(assassin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Split second permits mana abilities")
    void splitSecondPermitsManaAbilities() {
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        cast(List.of(elves.getId()));
        harness.ensurePriority(player2);

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can resolve with zero targets without destroying creatures")
    void resolvesWithZeroTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        harness.assertInGraveyard(player1, "V.A.T.S.");
    }

    @Test
    @DisplayName("A single target is destroyed even if its toughness changes")
    void destroysSingleTargetAfterToughnessChanges() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(bear.getId()));
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No targets are destroyed when the last target's toughness changes")
    void destroysNothingWhenLastTargetToughnessChanges() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()));
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "V.A.T.S.");
    }

    @Test
    @DisplayName("No targets are destroyed when the first target's toughness changes")
    void destroysNothingWhenFirstTargetToughnessChanges() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()));
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Any number permits more than ninety-nine targets")
    void destroysOneHundredEqualToughnessTargets() {
        List<UUID> targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .toList();
        prepareCard();

        harness.castAndResolveInstant(player1, 0, targets);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(100);
    }

    private void cast(List<UUID> targetIds) {
        prepareCard();
        harness.castInstant(player1, 0, targetIds);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new VATS()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
