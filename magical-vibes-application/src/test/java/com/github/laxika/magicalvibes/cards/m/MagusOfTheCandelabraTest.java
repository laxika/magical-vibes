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

    @Test
    @DisplayName("Rejects choosing the same land twice")
    void rejectsDuplicateTargets() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick, even for X=0")
    void rejectsSummoningSickSource() {
        harness.addToBattlefield(player1, new MagusOfTheCandelabra());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate an already tapped Magus")
    void rejectsTappedSource() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCandelabra());
        magus.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Must pay X mana")
    void rejectsInsufficientMana() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targets may be untapped and untapping waits for resolution")
    void allowsUntappedTargetsAndUsesStack() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(land.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(magus.isTapped()).isTrue();
        land.tap();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untaps remaining targets when another target leaves the battlefield")
    void untapsRemainingLegalTarget() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());
        first.tap();
        second.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.isTapped()).isFalse();
        assertThat(first.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose more than one hundred lands when X is larger")
    void allowsMoreThanOneHundredTargets() {
        addCreatureReady(player1, new MagusOfTheCandelabra());
        List<Permanent> lands = new java.util.ArrayList<>();
        for (int i = 0; i < 101; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
            land.tap();
            lands.add(land);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 101,
                lands.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(lands).allSatisfy(land -> assertThat(land.isTapped()).isFalse());
    }
}
