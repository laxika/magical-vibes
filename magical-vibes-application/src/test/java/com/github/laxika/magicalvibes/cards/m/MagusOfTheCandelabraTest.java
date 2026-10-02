package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheCandelabra.class, Forest.class, BenalishCavalry.class})
class MagusOfTheCandelabraTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps X target lands")
    void untapsXTargetLands() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(magus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects more targets than X")
    void rejectsMoreTargetsThanX() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects fewer targets than X")
    void rejectsFewerTargetsThanX() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows X=0 with no targets")
    void allowsZeroTargets() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCandelabra());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(magus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a non-land target")
    void rejectsNonLandTarget() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
