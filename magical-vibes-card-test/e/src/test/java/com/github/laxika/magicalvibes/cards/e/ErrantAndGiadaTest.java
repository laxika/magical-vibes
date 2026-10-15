package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AerialBoost;
import com.github.laxika.magicalvibes.cards.a.AvenReedstalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.cards.s.SaibaCryptomancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantAndGiada.class, AvenReedstalker.class, GrizzlyBears.class, StormCrow.class, SaibaCryptomancer.class, AerialBoost.class})
class ErrantAndGiadaTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a flying creature spell from the top of the library")
    void castsFlyingCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card stormCrow = new StormCrow();
        gd.playerDecks.get(player1.getId()).addFirst(stormCrow);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Storm Crow");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(stormCrow);
    }

    @Test
    @DisplayName("Can cast a flash creature spell from the top of the library")
    void castsFlashCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card avenReedstalker = new AvenReedstalker();
        gd.playerDecks.get(player1.getId()).addFirst(avenReedstalker);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Aven Reedstalker");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(avenReedstalker);
    }

    @Test
    @DisplayName("Cannot cast a creature without flying or flash from the top of the library")
    void cannotCastCreatureWithoutFlyingOrFlashFromLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    void castsFlashOnlySpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card cryptomancer = new SaibaCryptomancer();
        harness.setLibrary(player1, List.of(cryptomancer));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromLibraryTop(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getCard()).isSameAs(cryptomancer);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void flyingPermissionDoesNotGrantFlashTiming() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card crow = new StormCrow();
        harness.setLibrary(player1, List.of(crow));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(crow);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayCastMultipleEligibleSpellsInOneTurn() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card first = new StormCrow();
        Card second = new StormCrow();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Storm Crow")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void privatelyShowsEvenAnIneligibleTopCard() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    void losingAbilitiesRemovesCastingAndLookingPermissions() {
        var errantAndGiada = harness.addToBattlefieldAndReturn(player1, new ErrantAndGiada());
        errantAndGiada.setLosesAllAbilitiesUntilEndOfTurn(true);
        Card cryptomancer = new SaibaCryptomancer();
        harness.setLibrary(player1, List.of(cryptomancer));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cryptomancer);
        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Saiba Cryptomancer"));
    }

    @Test
    void opponentsCannotUseCastingPermission() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card cryptomancer = new SaibaCryptomancer();
        harness.setLibrary(player2, List.of(cryptomancer));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(cryptomancer);
    }

    @Test
    void failedManaPaymentLeavesSpellOnLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card cryptomancer = new SaibaCryptomancer();
        harness.setLibrary(player1, List.of(cryptomancer));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cryptomancer);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void instantSpeedAloneDoesNotQualifyAsFlash() {
        var errantAndGiada = harness.addToBattlefieldAndReturn(player1, new ErrantAndGiada());
        Card boost = new AerialBoost();
        harness.setLibrary(player1, List.of(boost));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, errantAndGiada.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(boost);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryCannotBeCastFrom() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
