package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({GoblinWarStrike.class, GoblinPiker.class, RagingGoblin.class, BearCub.class,
        GarrukWildspeaker.class})
class GoblinWarStrikeTest extends BaseCardTest {

    private void prepareCast() {
        harness.setHand(player1, List.of(new GoblinWarStrike()));
        harness.addMana(player1, ManaColor.RED, 1); // {R}
    }

    private void cast(UUID targetId) {
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Deals damage equal to the number of Goblins you control")
    void dealsDamageEqualToGoblinCount() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Deals no damage when you control no Goblins")
    void dealsNoDamageWithoutGoblins() {
        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("Counts only Goblins you control, not the opponent's")
    void countsOnlyControllersGoblins() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player2, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Does not count non-Goblin creatures")
    void doesNotCountNonGoblins() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new BearCub());

        int before = gd.getLife(player2.getId());
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player1.getId());
        cast(player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(before - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage to a target planeswalker")
    void dealsDamageToTargetPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        cast(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts Goblins when the spell resolves")
    void countsGoblinsAtResolution() {
        harness.addToBattlefield(player1, new GoblinPiker());
        prepareCast();
        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new RagingGoblin());

        int before = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Does not count Goblins that leave before resolution")
    void doesNotCountGoblinsThatLeaveBeforeResolution() {
        harness.addToBattlefield(player1, new GoblinPiker());
        prepareCast();
        harness.castSorcery(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(new GoblinPiker()));

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Goblin War Strike");
    }

    @Test
    @DisplayName("Counts tapped Goblins but not Goblin cards in other zones")
    void countsTappedGoblinsButNotCardsInOtherZones() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        goblin.tap();
        harness.setGraveyard(player1, List.of(new RagingGoblin()));
        harness.setExile(player1, List.of(new GoblinPiker()));
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        cast(player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player1, new GoblinPiker());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Goblin War Strike");
    }
}
