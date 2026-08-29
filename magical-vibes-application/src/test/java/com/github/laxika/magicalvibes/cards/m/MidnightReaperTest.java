package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature dying deals damage and draws a card")
    void anotherNontokenCreatureDies() {
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killCreatureWithShock(player2, player1, "Grizzly Bears");

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Midnight Reaper dying triggers its ability")
    void midnightReaperDies() {
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());

        killCreatureWithShock(player2, player1, "Midnight Reaper");

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Midnight Reaper");
    }

    @Test
    @DisplayName("A token creature dying does not trigger Midnight Reaper")
    void tokenCreatureDies() {
        harness.setHand(player1, List.of());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MidnightReaper());
        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        killCreatureWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    private void killCreatureWithShock(Player caster, Player targetController,
                                       String targetName) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
