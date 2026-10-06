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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootbornDefenses.class, DoomBlade.class, GrizzlyBears.class, Card.class})
class RootbornDefensesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control survive a destroy effect for the rest of the turn")
    void ownCreaturesGainIndestructible() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        doomBlade(player2, harness.getPermanentId(player1, "Grizzly Bears"));
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

        doomBlade(player2, harness.getPermanentId(player1, "Grizzly Bears"));
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
        doomBlade(player2, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent creatures do not gain indestructible")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(player1);

        doomBlade(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        assertThat(countOf(player2, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain indestructible")
    void laterCreatureIsNotProtected() {
        cast(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        doomBlade(player2, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Populate chooses one of multiple own tokens before protecting all creatures")
    void multipleTokensResumeBeforeGrantingIndestructible() {
        harness.addToBattlefield(player1, soldierToken());
        Card otherToken = soldierToken();
        otherToken.setName("Other Soldier Token");
        harness.addToBattlefield(player1, otherToken);
        harness.addToBattlefield(player2, soldierToken());
        UUID chosenId = harness.getPermanentId(player1, "Soldier Token");

        cast(player1);
        harness.handlePermanentChosen(player1, chosenId);

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(countOf(player1, "Other Soldier Token")).isEqualTo(1);
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
        for (Permanent creature : List.copyOf(gd.playerBattlefields.get(player1.getId()))) {
            doomBlade(player2, creature.getId());
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        doomBlade(player1, harness.getPermanentId(player2, "Soldier Token"));
        harness.assertNotOnBattlefield(player2, "Soldier Token");
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new RootbornDefenses()));
        harness.addMana(player, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player, 0);
    }

    private void doomBlade(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player, 0, targetId);
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
