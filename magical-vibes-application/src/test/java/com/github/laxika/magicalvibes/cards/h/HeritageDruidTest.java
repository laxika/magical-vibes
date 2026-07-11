package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeritageDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three untapped Elves adds {G}{G}{G}")
    void tapThreeElvesForGreenMana() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        Permanent elfA = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfB = addCreatureReady(player1, new ElvishWarrior());

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIdx, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(elfA.isTapped()).isTrue();
        assertThat(elfB.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three untapped Elves")
    void cannotActivateWithoutThreeElves() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        addCreatureReady(player1, new ElvishWarrior());

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIdx, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
}
