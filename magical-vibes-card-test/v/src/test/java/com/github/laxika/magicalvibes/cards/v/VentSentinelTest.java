package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.cards.w.WallOfOmens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VentSentinel.class, WallOfOmens.class, GlorySeeker.class, GideonJura.class, OneWithTheStars.class})
class VentSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of your creatures with defender")
    void dealsDamageEqualToControlledDefenderCount() {
        addReadySentinel();
        harness.addToBattlefield(player1, new WallOfOmens());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player2, new WallOfOmens());
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadySentinel();
        harness.addToBattlefield(player2, new GlorySeeker());
        addActivationMana();

        var targetId = harness.getPermanentId(player2, "Glory Seeker");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDamagePlaneswalker() {
        addReadySentinel();
        harness.addToBattlefield(player1, new WallOfOmens());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        addActivationMana();

        harness.activateAbility(player1, 0, null, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void canTargetControllerAndTapsAsCost() {
        Permanent sentinel = addReadySentinel();
        harness.setLife(player1, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(sentinel.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    void countsDefendersAtResolution() {
        addReadySentinel();
        harness.setLife(player2, 20);
        addActivationMana();
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.addToBattlefield(player1, new WallOfOmens());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void abilityResolvesWithZeroDefendersAfterSourceLeaves() {
        Permanent sentinel = addReadySentinel();
        harness.setLife(player2, 20);
        addActivationMana();
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(sentinel);
        gd.playerGraveyards.get(player1.getId()).add(sentinel.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent sentinel = addReadySentinel();
        sentinel.setSummoningSick(true);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent sentinel = addReadySentinel();
        sentinel.tap();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addReadySentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCountNoncreaturePermanentsWithDefender() {
        addReadySentinel();
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfOmens());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, wall.getId());
        harness.passBothPriorities();
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private Permanent addReadySentinel() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new VentSentinel());
        sentinel.setSummoningSick(false);
        return sentinel;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
