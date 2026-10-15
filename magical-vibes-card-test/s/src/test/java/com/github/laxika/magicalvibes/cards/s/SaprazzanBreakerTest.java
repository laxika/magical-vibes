package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Invigorate;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaprazzanBreaker.class, Forest.class, Invigorate.class, LeylineOfTheVoid.class})
class SaprazzanBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Milling a land makes Saprazzan Breaker unblockable this turn")
    void millingLandMakesItUnblockable() {
        Permanent breaker = addReadyBreaker();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(breaker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Milling a nonland does not make Saprazzan Breaker unblockable")
    void millingNonlandDoesNotMakeItUnblockable() {
        Permanent breaker = addReadyBreaker();
        Card nonland = new Invigorate();
        harness.setLibrary(player1, List.of(nonland));
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(breaker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Milling with an empty library does not make Saprazzan Breaker unblockable")
    void emptyLibraryDoesNotMakeItUnblockable() {
        Permanent breaker = addReadyBreaker();
        harness.setLibrary(player1, List.of());
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(breaker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent breaker = addReadyBreaker();
        harness.setLibrary(player1, List.of(new Forest()));
        addBlueMana();

        activateAndResolve(breaker);
        assertThat(breaker.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(breaker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A milled land diverted to exile still makes Saprazzan Breaker unblockable")
    void landMilledIntoExileMakesItUnblockable() {
        Permanent breaker = addReadyBreaker();
        Card forest = new Forest();
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(forest));
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(forest));
        assertThat(breaker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A later nonland mill does not remove the unblockable effect")
    void laterNonlandMillDoesNotRemoveUnblockable() {
        Permanent breaker = addReadyBreaker();
        Card forest = new Forest();
        Card nonland = new Invigorate();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateAndResolve(breaker);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(breaker.isCantBeBlocked()).isTrue();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, nonland);
        assertThat(breaker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new SaprazzanBreaker());
        breaker.tap();
        breaker.setSummoningSick(true);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addBlueMana();

        activateAndResolve(breaker);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(breaker.isCantBeBlocked()).isTrue();
        assertThat(breaker.isTapped()).isTrue();
    }

    private Permanent addReadyBreaker() {
        return addCreatureReady(player1, new SaprazzanBreaker());
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void activateAndResolve(Permanent breaker) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(breaker), null, null);
        harness.passBothPriorities();
    }
}
