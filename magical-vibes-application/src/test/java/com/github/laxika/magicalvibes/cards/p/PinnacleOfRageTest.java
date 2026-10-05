package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
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

@CardUsed({PinnacleOfRage.class, GrizzlyBears.class, GiantSpider.class, LilianaVess.class,
        SpringleafDrum.class})
class PinnacleOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each of two creature targets")
    void dealsDamageToEachCreatureTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId(), spider.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a player and a planeswalker")
    void targetsPlayerAndPlaneswalker() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(liliana.getId(), player2.getId()));

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Rejects duplicate targets")
    void rejectsDuplicateTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    void dealsThreeDamageToBothPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Pinnacle of Rage");
    }

    @Test
    void rejectsZeroTargets() {
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Pinnacle of Rage");
    }

    @Test
    void rejectsOnlyOneTarget() {
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Pinnacle of Rage");
    }

    @Test
    void rejectsThreeDistinctTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player2.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Pinnacle of Rage");
    }

    @Test
    void rejectsNoncreatureArtifactTarget() {
        Permanent drum = harness.addToBattlefieldAndReturn(player2, new SpringleafDrum());
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), drum.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Pinnacle of Rage");
    }

    @Test
    void stillDealsThreeDamageToRemainingLegalTarget() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, List.of(spider.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(spider);
        harness.setHand(player2, List.of(spider.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(spider.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Pinnacle of Rage");
    }

    @Test
    void doesNotResolveWhenBothTargetsLeaveBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new PinnacleOfRage()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, List.of(bears.getId(), spider.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).remove(spider);
        harness.setHand(player2, List.of(bears.getCard(), spider.getCard()));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(spider.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Pinnacle of Rage");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
    }
}
