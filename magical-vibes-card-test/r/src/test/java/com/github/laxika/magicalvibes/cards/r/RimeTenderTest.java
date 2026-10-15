package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.IcehideGolem;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimeTender.class, IcehideGolem.class, SnowCoveredForest.class, UniversalAutomaton.class})
class RimeTenderTest extends BaseCardTest {

    @Test
    void untapsAnotherTargetSnowPermanent() {
        Permanent tender = addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideGolem());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(tender.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNonsnowPermanent() {
        addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetItself() {
        Permanent tender = addCreatureReady(player1, new RimeTender());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tender.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapsControlledSnowLand() {
        addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetAnotherRimeTender() {
        Permanent source = addCreatureReady(player1, new RimeTender());
        Permanent target = addCreatureReady(player1, new RimeTender());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetAlreadyUntappedSnowPermanent() {
        Permanent source = addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideGolem());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new RimeTender());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideGolem());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void doesNotUntapOtherPermanentsWhenTargetLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new RimeTender());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideGolem());
        target.tap();
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        other.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
