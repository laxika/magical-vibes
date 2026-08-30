package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ShadowsLair;
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

@CardUsed({GraspingShadows.class, ShadowsLair.class, GrizzlyBears.class})
class GraspingShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Solo attacker gains deathtouch and lifelink and adds a dread counter")
    void soloAttackerGetsKeywordsAndAddsDreadCounter() {
        Permanent shadows = addShadows();
        Permanent attacker = addBear();

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
        assertThat(shadows.getCounterCount(CounterType.DREAD)).isEqualTo(1);
        assertThat(shadows.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Multiple attackers do not trigger Grasping Shadows")
    void multipleAttackersDoNotTrigger() {
        Permanent shadows = addShadows();
        Permanent firstAttacker = addBear();
        Permanent secondAttacker = addBear();

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, firstAttacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, firstAttacker, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondAttacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(shadows.getCounterCount(CounterType.DREAD)).isZero();
    }

    @Test
    @DisplayName("Third dread counter transforms Grasping Shadows")
    void thirdDreadCounterTransforms() {
        Permanent shadows = addShadows();
        shadows.setCounterCount(CounterType.DREAD, 2);
        addBear();

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(shadows.isTransformed()).isTrue();
        assertThat(shadows.getCard()).isInstanceOf(ShadowsLair.class);
        assertThat(shadows.getCounterCount(CounterType.DREAD)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shadows' Lair produces black mana")
    void backFaceProducesBlackMana() {
        Permanent lair = addLair();

        harness.activateAbility(player1, indexOf(lair), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shadows' Lair removes a dread counter to draw and lose life")
    void backFaceDrawsAndLosesLife() {
        Permanent lair = addLair();
        lair.setCounterCount(CounterType.DREAD, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(lair), 1, null, null);
        harness.passBothPriorities();

        assertThat(lair.getCounterCount(CounterType.DREAD)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private Permanent addShadows() {
        Permanent shadows = harness.addToBattlefieldAndReturn(player1, new GraspingShadows());
        shadows.setSummoningSick(false);
        return shadows;
    }

    private Permanent addBear() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        return bear;
    }

    private Permanent addLair() {
        GraspingShadows front = new GraspingShadows();
        Permanent lair = new Permanent(front);
        lair.setCard(front.getBackFaceCard());
        lair.setTransformed(true);
        lair.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lair);
        return lair;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
