package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustlerRampage.class, GrizzlyBears.class})
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

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
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
