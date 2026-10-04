package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BygoneColossus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HymnOfTheFaller.class, Forest.class, BygoneColossus.class})
class HymnOfTheFallerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils, then draws a card and loses 1 life")
    void surveilsThenDrawsAndLosesLife() {
        Card surveilled = new BygoneColossus();
        Card drawn = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(surveilled, drawn));
        harness.setHand(player1, List.of(new HymnOfTheFaller()));
        addMana();

        castHymn();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Void draws an additional card")
    void voidDrawsAdditionalCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BygoneColossus());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        Card surveilled = new BygoneColossus();
        Card firstDraw = new Forest();
        Card additionalDraw = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(surveilled, firstDraw, additionalDraw));
        harness.setHand(player1, List.of(new HymnOfTheFaller()));
        addMana();

        castHymn();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, additionalDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A land leaving the battlefield does not enable Void")
    void landLeavingBattlefieldDoesNotEnableVoid() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        Card surveilled = new BygoneColossus();
        Card drawn = new Forest();
        Card remaining = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(surveilled, drawn, remaining));
        harness.setHand(player1, List.of(new HymnOfTheFaller()));
        addMana();

        castHymn();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Keeping the surveilled card draws that card without enabling Void")
    void keepingSurveilledCardDrawsIt() {
        Card top = new BygoneColossus();
        Card remaining = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(top, remaining));
        harness.setHand(player1, List.of(new HymnOfTheFaller()));
        addMana();

        castHymn();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Warp enables Void before the warped permanent leaves the battlefield")
    void warpedSpellEnablesVoid() {
        harness.setHand(player1, List.of(new BygoneColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Card top = new Forest();
        Card additional = new Forest();
        Card remaining = new Forest();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(top, additional, remaining));
        harness.setHand(player1, List.of(new HymnOfTheFaller()));
        addMana();

        castHymn();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, additional);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player1, 19);
        harness.assertOnBattlefield(player1, "Bygone Colossus");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castHymn() {
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
