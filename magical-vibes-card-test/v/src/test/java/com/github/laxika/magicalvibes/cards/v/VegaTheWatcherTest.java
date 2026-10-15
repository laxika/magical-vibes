package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.b.BattleMammoth;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VegaTheWatcher.class, AncientGrudge.class, GoldveinPick.class, BattleMammoth.class})
class VegaTheWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from the graveyard draws a card")
    void castingFromGraveyardDrawsCard() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        harness.addToBattlefield(player1, new GoldveinPick());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0, harness.getPermanentId(player1, "Goldvein Pick"));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not draw a card")
    void castingFromHandDrawsNothing() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        harness.addToBattlefield(player1, new GoldveinPick());
        harness.setHand(player1, new ArrayList<>(List.of(new AncientGrudge())));
        harness.addMana(player1, ManaColor.RED, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Goldvein Pick"));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Foretelling does not draw, but casting the foretold spell draws before it resolves")
    void foretoldSpellDrawsBeforeResolving() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        BattleMammoth mammoth = new BattleMammoth();
        harness.setHand(player1, List.of(mammoth));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.foretell(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, mammoth.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Battle Mammoth")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Battle Mammoth")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("An opponent casting from the graveyard does not trigger Vega")
    void opponentsGraveyardSpellDoesNotDraw() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        harness.addToBattlefield(player1, new GoldveinPick());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player2, 0, harness.getPermanentId(player1, "Goldvein Pick"));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(countPermanents(player1, "Goldvein Pick")).isZero();
    }

    @Test
    @DisplayName("Casting a creature from hand does not trigger Vega")
    void creatureFromHandDoesNotDraw() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new BattleMammoth(), "{3}{G}{G}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Battle Mammoth")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
