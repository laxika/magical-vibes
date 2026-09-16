package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternOfUndersight.class, Forest.class, GrizzlyBears.class})
class LanternOfUndersightTest extends BaseCardTest {

    @Test
    void controllerDrawsFromBottomOfLibrary() {
        harness.addToBattlefield(player1, new LanternOfUndersight());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void doesNotAffectOpponentsDraws() {
        harness.addToBattlefield(player1, new LanternOfUndersight());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }
}
