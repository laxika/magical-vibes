package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CeaseDesist.class, Bonesplitter.class, GloriousAnthem.class, GrizzlyBears.class})
class CeaseDesistTest extends BaseCardTest {

    private static final int CEASE = 0;
    private static final int DESIST = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Cease exiles up to two cards from one graveyard and makes the target player gain life and draw")
    void ceaseExilesCardsGainsLifeAndDraws() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card left = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, left));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CEASE, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(left);
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Cease rejects a creature as its target player")
    void ceaseRequiresPlayerTarget() {
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, CEASE, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Desist destroys all artifacts and enchantments")
    void desistDestroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, DESIST, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Fuse resolves Cease before Desist")
    void fuseResolvesBothHalvesInOrder() {
        Card first = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Bonesplitter());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, FUSE, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }
}
