package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RootbornDefensesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control survive a destroy effect for the rest of the turn")
    void ownCreaturesGainIndestructible() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        doomBlade(player2, permanentOf(player1, "Grizzly Bears").getId());
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        doomBlade(player2, permanentOf(player1, "Grizzly Bears").getId());
        assertThat(countOf(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Populate happens first, so the new token copy is also indestructible")
    void populatedTokenIsIndestructible() {
        harness.addToBattlefield(player1, soldierToken());

        cast(player1);
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);

        for (Permanent soldier : soldiersOf(player1)) {
            doomBlade(player2, soldier.getId());
        }
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creature token, populate creates nothing but the grant still applies")
    void noTokenStillGrantsIndestructible() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        doomBlade(player2, permanentOf(player1, "Grizzly Bears").getId());
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent creatures do not gain indestructible")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(player1);

        doomBlade(player1, permanentOf(player2, "Grizzly Bears").getId());
        assertThat(countOf(player2, "Grizzly Bears")).isZero();
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new RootbornDefenses()));
        harness.addMana(player, ManaColor.WHITE, 3);
        harness.castInstant(player, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void doomBlade(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    private List<Permanent> soldiersOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Soldier Token".equals(p.getCard().getName()))
                .toList();
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
