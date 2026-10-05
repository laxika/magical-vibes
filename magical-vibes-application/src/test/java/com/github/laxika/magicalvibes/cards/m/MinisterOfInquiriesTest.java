package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinisterOfInquiries.class, Forest.class})
class MinisterOfInquiriesTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new MinisterOfInquiries()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysEnergyAndTapsToMillThreeCardsFromTargetPlayer() {
        Permanent minister = addCreatureReady(player1, new MinisterOfInquiries());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(minister.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void cannotActivateWithoutEnergy() {
        addCreatureReady(player1, new MinisterOfInquiries());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
    }

    @Test
    void cannotTargetAPermanent() {
        addCreatureReady(player1, new MinisterOfInquiries());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    void canMillItsControllerAndPaysCostsBeforeResolution() {
        Permanent minister = addCreatureReady(player1, new MinisterOfInquiries());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, remaining));

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(minister.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third, remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void millsAllRemainingCardsFromAShortLibrary() {
        addCreatureReady(player1, new MinisterOfInquiries());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void canTargetAnEmptyLibrary() {
        Permanent minister = addCreatureReady(player1, new MinisterOfInquiries());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(minister.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new MinisterOfInquiries());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent minister = addCreatureReady(player1, new MinisterOfInquiries());
        minister.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }
}
