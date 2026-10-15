package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.e.EmergeUnscathed;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForkedBolt.class, GlorySeeker.class, GideonJura.class, EmergeUnscathed.class, PropheticPrism.class})
class ForkedBoltTest extends BaseCardTest {

    @Test
    void cannotAssignZeroDamageToAChosenTarget() {
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAssignLessThanTwoDamage() {
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithoutTargets() {
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANoncreatureArtifact() {
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(prism.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoDamageKillsATargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.assertInGraveyard(player1, "Forked Bolt");
    }

    @Test
    void canDivideDamageBetweenBothPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void canDamageAPlaneswalker() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, Map.of(gideon.getId(), 2));
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void doesNotRedistributeDamageWhenOneTargetGainsProtection() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.setHand(player2, List.of(new EmergeUnscathed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 1, player2.getId(), 1));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleListChoice(player2, "RED");
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Forked Bolt");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetGainsProtection() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.setHand(player2, List.of(new EmergeUnscathed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 2));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleListChoice(player2, "RED");
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player1, "Forked Bolt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsTwoDamageToOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void dividesDamageAmongTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId())
                        && permanent.getMarkedDamage() == 1);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent bears3 = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(
                bears1.getId(), 1,
                bears2.getId(), 1,
                bears3.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }
}
