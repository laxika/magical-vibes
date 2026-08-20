package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoomskarTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures but not other permanents")
    void destroysAllCreaturesButNotOtherPermanents() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new Doomskar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Doomskar");
    }

    @Test
    @DisplayName("Foretell exiles Doomskar face down and allows casting it on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Doomskar doomskar = new Doomskar();
        harness.setHand(player1, List.of(doomskar));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(doomskar.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.exiledTurnNumber()).isEqualTo(gd.turnNumber);
        assertThat(gd.foretoldCardIds).contains(doomskar.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.castFromExile(player1, doomskar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, doomskar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doomskar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.foretoldCardIds).doesNotContain(doomskar.getId());
    }
}
