package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishSpiritGuide;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitsuneDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target Spirit and pays the tap cost")
    void tapsTargetSpirit() {
        Permanent diviner = addReadyDiviner(player1);
        Permanent spirit = addReadyPermanent(player2, new ElvishSpiritGuide());

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        assertThat(diviner.isTapped()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Spirit permanent")
    void cannotTargetNonSpirit() {
        addReadyDiviner(player1);
        Permanent bears = addReadyPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDiviner(Player player) {
        return addReadyPermanent(player, new KitsuneDiviner());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
