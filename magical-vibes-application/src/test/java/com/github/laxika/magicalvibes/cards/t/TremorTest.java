package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tremor.class, RagingGoblin.class, WindDrake.class, Warthog.class, Mountain.class})
class TremorTest extends BaseCardTest {

    @Test
    @DisplayName("Kills ground creatures on both sides")
    void killsGroundCreatures() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageFlyers() {
        harness.addToBattlefield(player2, new WindDrake());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Deals exactly 1 damage to a larger ground creature")
    void dealsOneDamageToLargerGroundCreature() {
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new Warthog());
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Warthog");
        assertThat(warthog.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyard() {
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tremor");
    }
}
