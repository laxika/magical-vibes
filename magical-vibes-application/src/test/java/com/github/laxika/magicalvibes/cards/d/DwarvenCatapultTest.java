package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("X=6 among 3 creatures deals 2 damage to each")
    void dividesEvenlyAmongCreatures() {
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 7); // {6}{R}

        harness.castInstant(player1, 0, 6, player2.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(3);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("X=7 among 3 creatures rounds down to 2 damage each")
    void roundsDown() {
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 8); // {7}{R}

        harness.castInstant(player1, 0, 7, player2.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(3);
        // floor(7/3) = 2 damage each
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Kills creatures dealt lethal damage")
    void killsCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 7); // {6}{R}

        // floor(6/2) = 3 damage each — both 2/2 die
        harness.castInstant(player1, 0, 6, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears")).hasSize(2);
    }

    @Test
    @DisplayName("Does not damage the targeted player")
    void doesNotDamagePlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, 6, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does nothing when target opponent controls no creatures")
    void noCreatures() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, 6, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCasterCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, 6, player2.getId());
        harness.passBothPriorities();

        Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DwarvenCatapult()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 6, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
