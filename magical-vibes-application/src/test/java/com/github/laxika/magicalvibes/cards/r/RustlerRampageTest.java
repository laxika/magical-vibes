package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustlerRampage.class, SterlingHound.class, Plains.class})
class RustlerRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Untap mode untaps all creatures controlled by the target player")
    void untapModeUntapsTargetPlayersCreatures() {
        Permanent targetFirst = addTappedCreature(player2);
        Permanent targetSecond = addTappedCreature(player2);
        Permanent notTargeted = addTappedCreature(player1);

        cast(new int[]{0}, List.of(player2.getId()), 2);

        assertThat(targetFirst.isTapped()).isFalse();
        assertThat(targetSecond.isTapped()).isFalse();
        assertThat(notTargeted.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Double-strike mode grants double strike to the target creature")
    void doubleStrikeModeGrantsDoubleStrike() {
        Permanent target = addCreatureReady(player2);

        cast(new int[]{1}, List.of(target.getId()), 2);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Both modes each cost one additional mana and resolve")
    void bothModesResolve() {
        Permanent target = addTappedCreature(player2);

        cast(new int[]{0, 1}, List.of(player2.getId(), target.getId()), 3);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Each mode enforces its own target type")
    void modesRejectWrongTargetTypes() {
        harness.setHand(player1, List.of(new RustlerRampage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(addCreatureReady(player2).getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doubleStrikeExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player1);

        cast(new int[]{1}, List.of(target.getId()), 2);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void untapModeCanTargetItsController() {
        Permanent own = addTappedCreature(player1);
        Permanent opposing = addTappedCreature(player2);

        cast(new int[]{0}, List.of(player1.getId()), 2);

        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doubleStrikeModeDoesNotUntapCreatures() {
        Permanent target = addTappedCreature(player2);
        Permanent other = addTappedCreature(player2);

        cast(new int[]{1}, List.of(target.getId()), 2);

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void bothModesRequireBothAdditionalManaPayments() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new RustlerRampage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doubleStrikeModeRejectsPlayerTarget() {
        harness.setHand(player1, List.of(new RustlerRampage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapModeStillResolvesWhenCreatureTargetLeavesBattlefield() {
        Permanent target = addTappedCreature(player2);
        Permanent remaining = addTappedCreature(player2);
        harness.setHand(player1, List.of(new RustlerRampage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(player2.getId(), target.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untapModeLeavesNoncreaturesTapped() {
        Permanent creature = addTappedCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        land.tap();

        cast(new int[]{0}, List.of(player2.getId()), 2);

        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void doubleStrikeModeRejectsNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new RustlerRampage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new SterlingHound());
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addCreatureReady(player);
        creature.tap();
        return creature;
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new RustlerRampage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
