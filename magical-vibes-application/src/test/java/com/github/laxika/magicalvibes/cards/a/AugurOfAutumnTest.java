package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CatharCommando;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.StitchedDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AugurOfAutumn.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        CatharCommando.class, Consider.class, StitchedDrake.class})
class AugurOfAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from the top of the library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Coven allows casting a creature from the top of the library")
    void castsCreatureFromLibraryTopWithCoven() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot cast a creature from the top without Coven")
    void cannotCastCreatureFromLibraryTopWithoutCoven() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    void privatelyShowsTopCardWithoutCovenOrPriority() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.setLibrary(player1, List.of(new Consider()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Consider"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Consider"));
    }

    @Test
    void showsNewTopCardAfterPlayingLand() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.setLibrary(player1, List.of(new Forest(), new Consider()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Consider"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Consider"));
    }

    @Test
    void doesNotGrantAdditionalLandPlay() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayLandDuringUpkeep() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void threeCreaturesWithOnlyTwoDifferentPowersDoNotEnableCoven() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void opponentsCreaturesDoNotEnableCoven() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void covenUsesPowerIncludingCounters() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        var battlefieldBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        battlefieldBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        GrizzlyBears topBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topBears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topBears);
    }

    @Test
    void allowsMultipleCreatureCastsEvenWithDuplicatePowers() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastNoncreatureSpellWithCoven() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        Consider consider = new Consider();
        harness.setLibrary(player1, List.of(consider));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(consider);
    }

    @Test
    void covenDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void castsFlashCreatureDuringOpponentsTurnWithCoven() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new CatharCommando()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Cathar Commando");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void failedManaPaymentKeepsTopCardInLibrary() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(bears, nextCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, nextCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeginCastingCreatureWithPayableAdditionalCost() {
        harness.addToBattlefield(player1, new AugurOfAutumn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatCode(() -> harness.castFromLibraryTop(player1)).doesNotThrowAnyException();
    }
}
