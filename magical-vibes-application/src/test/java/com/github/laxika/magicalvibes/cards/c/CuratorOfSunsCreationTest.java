package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrimordialGnawer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuratorOfSunsCreation.class, PrimordialGnawer.class, Shock.class, Forest.class,
        GrizzlyBears.class, CentaurCourser.class})
class CuratorOfSunsCreationTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers again for the same value and only triggers once each turn")
    void discoversAgainForSameValueOnceEachTurn() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new CuratorOfSunsCreation());
        Permanent gnawer = harness.addToBattlefieldAndReturn(player1, new PrimordialGnawer());
        GrizzlyBears firstDiscovered = new GrizzlyBears();
        CentaurCourser secondDiscovered = new CentaurCourser();
        harness.setLibrary(player1, List.of(new Forest(), firstDiscovered, new Forest(), secondDiscovered));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, gnawer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        chooseDiscoveredCard(-1);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == curator.getCard());

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch secondDiscover =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondDiscover).isNotNull();
        assertThat(secondDiscover.params().cards()).containsExactly(secondDiscovered);

        chooseDiscoveredCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == curator.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDiscovered, secondDiscovered);
    }

    private void chooseDiscoveredCard(int cardIndex) {
        harness.handleCardChosen(player1, cardIndex);
    }
}
