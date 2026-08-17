package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GarrisonSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Has double strike while its controller controls a Gate")
    void hasDoubleStrikeWithGate() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        harness.addToBattlefield(player1, createGate());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have double strike without a Gate")
    void noDoubleStrikeWithoutGate() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Gate does not grant double strike")
    void opponentGateDoesNotCount() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        harness.addToBattlefield(player2, createGate());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when the Gate leaves the battlefield")
    void losesDoubleStrikeWhenGateLeaves() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, createGate());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(gate);

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Card createGate() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.GATE));
        return card;
    }
}
