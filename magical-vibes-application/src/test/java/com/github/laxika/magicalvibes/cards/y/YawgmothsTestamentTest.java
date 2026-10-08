package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.TreasureCruise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YawgmothsTestament.class, DarkRitual.class, Forest.class, GrizzlyBears.class, SwordsToPlowshares.class, TreasureCruise.class})
class YawgmothsTestamentTest extends BaseCardTest {

    @Test
    void playsFaceUpCardsFromExile() {
        YawgmothsTestament testament = new YawgmothsTestament();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(testament));
        harness.setExile(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void putsCardsOnBottomOfLibraryInsteadOfGraveyardOrExile() {
        YawgmothsTestament testament = new YawgmothsTestament();
        DarkRitual ritual = new DarkRitual();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(testament));
        harness.setExile(player1, List.of(ritual));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castFromExile(player1, ritual.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, testament, ritual);
    }

    @Test
    void cannotPlayFaceDownCardsFromExile() {
        DarkRitual ritual = new DarkRitual();
        gd.addToExile(player1.getId(), ritual, null, true);
        resolveTestament();

        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, ritual.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ritual);
    }

    @Test
    void cannotPlayCardsOwnedByOpponent() {
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player2, List.of(ritual));
        resolveTestament();

        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, ritual.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(ritual);
    }

    @Test
    void mustPayManaCostForExiledSpell() {
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        resolveTestament();

        assertThatThrownBy(() -> harness.castFromExile(player1, ritual.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ritual);
    }

    @Test
    void doesNotGrantAdditionalLandPlays() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setExile(player1, List.of(first, second));
        resolveTestament();

        harness.castFromExile(player1, first.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void replacesExileWithLibraryBottomAndStillGainsLife() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest top = new Forest();
        harness.addToBattlefield(player1, bears);
        harness.setLibrary(player1, List.of(top));
        harness.setLife(player1, 20);
        YawgmothsTestament testament = resolveTestament();
        SwordsToPlowshares swords = new SwordsToPlowshares();
        harness.setHand(player1, List.of(swords));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, testament, bears, swords);
        harness.assertLife(player1, 22);
    }

    @Test
    void doesNotReplaceOpponentsGraveyardMoves() {
        resolveTestament();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player2, List.of(ritual));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ritual);
    }

    @Test
    void canCastDelveSpellFromExileByPayingFullManaCost() {
        TreasureCruise cruise = new TreasureCruise();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setExile(player1, List.of(cruise));
        resolveTestament();
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castFromExile(player1, cruise.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).last().isSameAs(cruise);
    }

    @Test
    void stillRequiresNormalCreatureSpellTiming() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setExile(player1, List.of(bears));
        resolveTestament();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    void bothEffectsExpireAtEndOfTurn() {
        DarkRitual exiledRitual = new DarkRitual();
        DarkRitual handRitual = new DarkRitual();
        harness.setExile(player1, List.of(exiledRitual));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        YawgmothsTestament testament = resolveTestament();
        harness.setHand(player1, List.of(handRitual));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledRitual.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledRitual);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(handRitual);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(testament);
    }
    private YawgmothsTestament resolveTestament() {
        YawgmothsTestament testament = new YawgmothsTestament();
        harness.setHand(player1, List.of(testament));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        return testament;
    }
}
