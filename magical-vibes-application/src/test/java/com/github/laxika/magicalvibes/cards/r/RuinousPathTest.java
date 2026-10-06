package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CliffsideLookout;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonAllyOfZendikar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuinousPath.class, Forest.class, CliffsideLookout.class, GideonAllyOfZendikar.class})
class RuinousPathTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        castNormallyAt(target);

        harness.assertNotOnBattlefield(player2, "Cliffside Lookout");
        harness.assertInGraveyard(player2, "Cliffside Lookout");
    }

    @Test
    void destroysTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GideonAllyOfZendikar());
        target.setCounterCount(CounterType.LOYALTY, 4);
        castNormallyAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void alternateCastAwakensTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RuinousPath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cliffside Lookout");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotTargetLandForDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RuinousPath()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        harness.setHand(player1, List.of(new RuinousPath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void awakenStillResolvesWhenDestructionTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castWithAwaken(target, land);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
    }

    @Test
    void destroysCreatureWhenAwakenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castWithAwaken(target, land);
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cliffside Lookout");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotAwakenLandThatOpponentControlsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castWithAwaken(target, land);
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cliffside Lookout");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void awakeningSameLandAgainAddsCountersWithoutResettingThem() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castWithAwaken(firstTarget, land);
        harness.passBothPriorities();
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        castWithAwaken(secondTarget, land);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
    }

    @Test
    void canChooseSameCreatureLandForDestructionAndAwaken() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castWithAwaken(creature, land);
        harness.passBothPriorities();

        castWithAwaken(land, land);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotAwakenOpponentsLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castWithAwaken(creature, land))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithAwaken(Permanent target, Permanent land) {
        harness.setHand(player1, List.of(new RuinousPath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), land.getId()));
    }

    private void castNormallyAt(Permanent target) {
        harness.setHand(player1, List.of(new RuinousPath()));
        addNormalMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
