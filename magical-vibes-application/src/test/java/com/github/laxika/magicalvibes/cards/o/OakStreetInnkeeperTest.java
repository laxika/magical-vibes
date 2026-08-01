package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OakStreetInnkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creature you control has hexproof during an opponent's turn")
    void tappedCreatureHasHexproofOnOpponentTurn() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Untapped creature you control has no hexproof during an opponent's turn")
    void untappedCreatureHasNoHexproof() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Tapped creature has no hexproof during your own turn")
    void noHexproofDuringOwnTurn() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Oak Street Innkeeper grants hexproof to itself while tapped on an opponent's turn")
    void grantsHexproofToItself() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        Permanent innkeeper = findPermanent(player1, "Oak Street Innkeeper");
        innkeeper.tap();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, innkeeper, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent's tapped creatures do not gain hexproof")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        opponentBears.tap();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Hexproof is gone once Oak Street Innkeeper leaves the battlefield")
    void hexproofGoneWhenSourceLeaves() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();
        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Oak Street Innkeeper"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target a tapped creature you control on their turn")
    void opponentCannotTargetTappedCreature() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Opponent can target the same creature on your turn")
    void opponentCanTargetOnYourTurn() {
        harness.addToBattlefield(player1, new OakStreetInnkeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        gs.playCard(gd, player2, 0, 0, bears.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }
}
