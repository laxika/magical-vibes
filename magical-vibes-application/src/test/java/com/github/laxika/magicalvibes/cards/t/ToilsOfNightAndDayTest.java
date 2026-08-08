package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToilsOfNightAndDayTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting both prompts toggles each target independently")
    void togglesBothTargets() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent forest = addLand(player1);
        forest.tap();

        castToils(bears, forest);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the first prompt leaves that permanent untouched")
    void decliningFirstOnlyAffectsSecond() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent forest = addLand(player1);
        forest.tap();

        castToils(bears, forest);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining both prompts changes nothing")
    void decliningBothDoesNothing() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent forest = addLand(player1);
        forest.tap();

        castToils(bears, forest);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second target must be another permanent")
    void rejectsTheSamePermanentTwice() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castToils(Permanent first, Permanent second) {
        prepareCast();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ToilsOfNightAndDay()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
