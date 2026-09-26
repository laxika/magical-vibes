package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsuneDiviner.class, WanderingOnes.class, IsamaruHoundOfKonda.class})
class KitsuneDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target Spirit and pays the tap cost")
    void tapsTargetSpirit() {
        Permanent diviner = addReadyDiviner(player1);
        Permanent spirit = addCreatureReady(player2, new WanderingOnes());

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        assertThat(diviner.isTapped()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Spirit permanent")
    void cannotTargetNonSpirit() {
        addReadyDiviner(player1);
        Permanent dog = addCreatureReady(player2, new IsamaruHoundOfKonda());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dog.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a tapped Spirit")
    void canTargetTappedSpirit() {
        Permanent diviner = addReadyDiviner(player1);
        Permanent spirit = addCreatureReady(player2, new WanderingOnes());
        spirit.tap();

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        assertThat(diviner.isTapped()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while the source has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new KitsuneDiviner());
        Permanent spirit = addCreatureReady(player2, new WanderingOnes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spirit.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDiviner(Player player) {
        return addCreatureReady(player, new KitsuneDiviner());
    }
}
