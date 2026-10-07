package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeLordRegeneration.class, TheFourthDoctor.class, TheTenthDoctor.class,
        DoomBlade.class, GrizzlyBears.class, SwordsToPlowshares.class,
        ArtificialEvolution.class, Lignify.class})
class TimeLordRegenerationTest extends BaseCardTest {

    @Test
    void timeLordReturnsAnotherTimeLordFromLibraryWhenItDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card nonmatchingCard = new GrizzlyBears();
        Card foundCard = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(nonmatchingCard, foundCard));

        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == foundCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
    }

    @Test
    void cannotTargetNonTimeLordYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .hasMessageContaining("Time Lord");
    }

    @Test
    void cannotTargetOpponentsTimeLord() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheFourthDoctor());
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stopsAtFirstTimeLordAndPutsSkippedCardsBelowUnrevealedCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card skippedFirst = new TimeLordRegeneration();
        Card skippedSecond = new SwordsToPlowshares();
        Card found = new TheTenthDoctor();
        Card unrevealed = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(skippedFirst, skippedSecond, found, unrevealed));
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedFirst, skippedSecond);
    }

    @Test
    void noMatchingCardReturnsEntireLibraryWithoutPuttingAnythingOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card first = new TimeLordRegeneration();
        Card second = new SwordsToPlowshares();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player1, "The Fourth Doctor");
    }

    @Test
    void emptyLibraryDoesNotPreventCreatureFromDying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "The Fourth Doctor");
    }

    @Test
    void exilingTimeLordDoesNotTriggerRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card found = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(found);
    }

    @Test
    void twoRegenerationsGrantTwoIndependentDeathTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card first = new TheTenthDoctor();
        Card second = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TimeLordRegeneration(), new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card found = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(found);
        harness.assertInGraveyard(player1, "The Fourth Doctor");
    }

    @Test
    void canTargetNoncreatureKindredTimeLordYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TheFourthDoctor());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        UUID auraId = harness.getPermanentId(player1, "Lignify");

        harness.setHand(player1, List.of(new ArtificialEvolution(), new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, auraId);
        harness.handleListChoice(player1, "TREEFOLK");
        harness.handleListChoice(player1, "TIME_LORD");

        harness.castInstant(player1, 0, auraId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(auraId);
    }
}
