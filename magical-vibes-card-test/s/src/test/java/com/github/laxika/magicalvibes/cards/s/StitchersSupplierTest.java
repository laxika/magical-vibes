package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StitchersSupplier.class, Forest.class})
class StitchersSupplierTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StitchersSupplier()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("ETB mills three cards from the controller's library")
    void etbMillsThree() {
        harness.setLibrary(player1, List.of(
                new StitchersSupplier(), new Forest(), new Forest(), new Forest(), new Forest()));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Dies: mills three cards")
    void diesMillsThree() {
        Card top1 = new Forest();
        Card top2 = new Forest();
        Card top3 = new Forest();
        Card top4 = new Forest();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4));
        harness.addToBattlefield(player1, new StitchersSupplier());
        Permanent supplier = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, supplier));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Stitcher's Supplier"))
                .anyMatch(c -> c.getId().equals(top1.getId()))
                .anyMatch(c -> c.getId().equals(top2.getId()))
                .anyMatch(c -> c.getId().equals(top3.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top4);
    }

    @Test
    @DisplayName("ETB does not mill the opponent")
    void etbDoesNotMillOpponent() {
        castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB mills all remaining cards when fewer than three remain")
    void etbWithShortLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("ETB with an empty library does not lose the game")
    void etbWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Returning Supplier to hand does not trigger its death ability")
    void returningToHandDoesNotMill() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new StitchersSupplier());
        Permanent supplier = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, supplier));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(supplier.getCard());
    }

    @Test
    @DisplayName("Death before ETB resolves still mills three cards for each trigger")
    void diesBeforeEtbResolves() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StitchersSupplier()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent supplier = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card supplierCard = supplier.getCard();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, supplier));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(6));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(supplierCard, library.get(0), library.get(1), library.get(2),
                        library.get(3), library.get(4), library.get(5));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
