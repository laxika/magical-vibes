package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wrench;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CeaseDesist.class, Bonesplitter.class, GloriousAnthem.class, GrizzlyBears.class,
        CuriousCadaver.class, Wrench.class, CaseOfTheShatteredPact.class})
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

        harness.castAndResolveSorcery(player1, 0, DESIST);

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cease // Desist cannot cast both halves together")
    void cannotFuseHalves() {
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, FUSE, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cease gains life and draws with both graveyards empty")
    void ceaseWithEmptyGraveyards() {
        Card drawn = new CuriousCadaver();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CEASE, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Cease can choose zero graveyard cards and target its controller")
    void ceaseCanChooseZeroCards() {
        Card untouched = new CuriousCadaver();
        Card drawn = new CuriousCadaver();
        harness.setGraveyard(player2, List.of(untouched));
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, CEASE, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Cease rejects cards from different graveyards")
    void ceaseRequiresSingleGraveyard() {
        Card first = new CuriousCadaver();
        Card second = new CuriousCadaver();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CEASE, player2.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Desist cannot be cast during the opponent's turn")
    void desistRequiresSorceryTiming() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, DESIST, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cease can be cast during the opponent's turn")
    void ceaseHasInstantTiming() {
        Card drawn = new CuriousCadaver();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new CuriousCadaver()));
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, CEASE, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Desist destroys both players' artifacts and enchantments but spares other creatures")
    void desistDestroysBothBattlefieldsAndSparesCreatures() {
        harness.addToBattlefield(player1, new Wrench());
        harness.addToBattlefield(player2, new Wrench());
        harness.addToBattlefield(player1, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player2, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player1, new CuriousCadaver());
        harness.addToBattlefield(player2, new CuriousCadaver());
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, DESIST);

        harness.assertNotOnBattlefield(player1, "Wrench");
        harness.assertNotOnBattlefield(player2, "Wrench");
        harness.assertNotOnBattlefield(player1, "Case of the Shattered Pact");
        harness.assertNotOnBattlefield(player2, "Case of the Shattered Pact");
        harness.assertInGraveyard(player1, "Wrench");
        harness.assertInGraveyard(player2, "Wrench");
        harness.assertInGraveyard(player1, "Case of the Shattered Pact");
        harness.assertInGraveyard(player2, "Case of the Shattered Pact");
        harness.assertOnBattlefield(player1, "Curious Cadaver");
        harness.assertOnBattlefield(player2, "Curious Cadaver");
    }

    @Test
    @DisplayName("Cease's player target is independent of the chosen graveyard")
    void ceaseCanExileFromDifferentPlayersGraveyard() {
        Card exiled = new CuriousCadaver();
        Card drawn = new CuriousCadaver();
        harness.setGraveyard(player1, List.of(exiled));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CeaseDesist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, CEASE, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Cease // Desist");
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .containsExactly(exiled.getId());
    }
}
