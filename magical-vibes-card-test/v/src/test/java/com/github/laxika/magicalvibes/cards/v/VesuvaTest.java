package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CalciformPools;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vesuva.class, Forest.class, CalciformPools.class})
class VesuvaTest extends BaseCardTest {

    @Test
    @DisplayName("Vesuva can enter tapped as a copy of a land")
    void entersTappedAsCopyOfLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());

        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vesuva.getCard().getName()).isEqualTo("Forest");
        assertThat(vesuva.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vesuva enters untapped when its copy choice is declined")
    void entersUntappedWhenCopyIsDeclined() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vesuva.getCard().getName()).isEqualTo("Vesuva");
        assertThat(vesuva.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vesuva enters untapped without a choice when there are no lands to copy")
    void entersWithoutCopyWhenNoLandsExist() {
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Vesuva");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vesuva copies a Forest's mana ability and untaps normally")
    void copiedForestProducesGreenMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.performUntapStep(player1);

        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vesuva.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);

        assertThat(vesuva.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vesuva copies a nonbasic land's ability without copying its counters")
    void copiesNonbasicLandWithoutCounters() {
        Permanent pools = harness.addToBattlefieldAndReturn(player2, new CalciformPools());
        pools.setCounterCount(CounterType.STORAGE, 3);
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, pools.getId());

        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vesuva.isTapped()).isTrue();
        assertThat(vesuva.getCounterCount(CounterType.STORAGE)).isZero();
        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pools.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
    }
}
