package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.f.FlamekinBrawler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindSpotGiant.class, AxegrinderGiant.class, AvianChangeling.class, FlamekinBrawler.class})
class BlindSpotGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controlling another Giant")
    void canAttackWithAnotherGiant() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when it is the only Giant controlled")
    void cannotAttackAsOnlyGiant() {
        addCreatureReady(player1, new BlindSpotGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the opponent controls another Giant")
    void cannotAttackWhenOnlyOpponentControlsGiant() {
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player2, new AxegrinderGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling another Giant")
    void canBlockWithAnotherGiant() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when it is the only Giant controlled")
    void cannotBlockAsOnlyGiant() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new BlindSpotGiant());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Another Blind-Spot Giant allows both Giants to attack")
    void twoBlindSpotGiantsCanAttackTogether() {
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new BlindSpotGiant());

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> permanent.isAttacking());
    }

    @Test
    @DisplayName("A tapped Giant with summoning sickness still enables attacking")
    void tappedSummoningSickGiantEnablesAttack() {
        addCreatureReady(player1, new BlindSpotGiant());
        var support = addCreatureReady(player1, new AxegrinderGiant());
        support.setTapped(true);
        support.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A changeling enables attacking")
    void changelingEnablesAttack() {
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new AvianChangeling());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A non-Giant does not enable attacking")
    void nonGiantDoesNotEnableAttack() {
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new FlamekinBrawler());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing Giant does not enable blocking")
    void opposingGiantDoesNotEnableBlock() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new FlamekinBrawler());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A changeling enables blocking")
    void changelingEnablesBlock() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new AvianChangeling());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }
}
