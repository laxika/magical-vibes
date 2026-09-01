package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ultima.class, DarksteelRelic.class, Forest.class, GrizzlyBears.class, HowlingMine.class, Ornithopter.class})
class UltimaTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts and creatures before ending the turn")
    void destroysArtifactsAndCreaturesThenEndsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new HowlingMine());

        int turnBefore = gd.turnNumber;
        harness.setHand(player1, List.of(new Ultima()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ultima"));
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
    }

    @Test
    @DisplayName("Indestructible artifacts survive")
    void indestructibleArtifactsSurvive() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new Ultima()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Relic");
    }
}
