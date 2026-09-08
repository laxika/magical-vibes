package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenthDistrictVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps another tapped creature you control")
    void attackUntapsAnotherCreatureYouControl() {
        Permanent veteran = addReady(player1, new TenthDistrictVeteran());
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(veteran.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger cannot target the attacking Veteran")
    void cannotTargetItself() {
        Permanent veteran = addReady(player1, new TenthDistrictVeteran());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, veteran.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack trigger cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReady(player1, new TenthDistrictVeteran());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
