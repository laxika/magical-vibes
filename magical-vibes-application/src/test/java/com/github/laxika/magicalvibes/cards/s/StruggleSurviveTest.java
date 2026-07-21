package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StruggleSurviveTest extends BaseCardTest {

    @Test
    @DisplayName("Struggle deals damage equal to lands you control")
    void struggleDealsDamageEqualToControlledLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StruggleSurvive()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Struggle"));
    }

    @Test
    @DisplayName("Struggle counts only your lands")
    void struggleCountsOnlyControllerLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StruggleSurvive()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Struggle cannot target a non-creature")
    void struggleCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StruggleSurvive()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Survive from graveyard shuffles each player's graveyard into their library, then exiles")
    void surviveFlashbackShufflesGraveyardsAndExiles() {
        Card p1GyCard = new Mountain();
        Card p2GyCard = new Forest();
        setDeck(player1, List.of(new Mountain(), new Forest()));
        setDeck(player2, List.of(new Mountain(), new Forest()));
        harness.setGraveyard(player1, List.of(new StruggleSurvive(), p1GyCard));
        harness.setGraveyard(player2, List.of(p2GyCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(p1GyCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(p2GyCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Struggle"));
    }

    @Test
    @DisplayName("Survive with empty graveyards still shuffles libraries and exiles")
    void surviveWithEmptyGraveyardsStillShufflesAndExiles() {
        setDeck(player1, List.of(new Mountain(), new Forest()));
        setDeck(player2, List.of(new Mountain()));
        harness.setGraveyard(player1, List.of(new StruggleSurvive()));
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Struggle"));
    }

    @Test
    @DisplayName("Survive requires sorcery timing")
    void surviveRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new StruggleSurvive()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
