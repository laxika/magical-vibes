package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutFanaticalPriestess.class, GrizzlyBears.class, LlanowarElves.class,
        Plains.class, Island.class, Swamp.class, Mountain.class, Forest.class})
class GutFanaticalPriestessTest extends BaseCardTest {

    @Test
    void etbFightExilesTheOpposingCreatureWithGut() {
        Permanent gut = castGutAndResolveFight();

        assertThat(gd.getCardsExiledByPermanent(gut.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void whiteSpecializationCreatesTwoHastyTwoTwoCopies() {
        List<Permanent> tokens = specializeGut(new Plains(), 0);

        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        });
    }

    @Test
    void blueSpecializationCreatesAThreeThreeFlyingCopy() {
        List<Permanent> tokens = specializeGut(new Island(), 1);

        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.HASTE);
        });
    }

    @Test
    void blackSpecializationCreatesAFourFourMenaceCopy() {
        List<Permanent> tokens = specializeGut(new Swamp(), 2);

        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(4);
            assertThat(token.getCard().getToughness()).isEqualTo(4);
            assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE, Keyword.HASTE);
        });
    }

    @Test
    void redSpecializationCreatesADoubleStrikeCopy() {
        List<Permanent> tokens = specializeGut(new Mountain(), 3);

        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.DOUBLE_STRIKE, Keyword.HASTE);
        });
    }

    @Test
    void greenSpecializationCreatesAFiveFiveTrampleCopy() {
        List<Permanent> tokens = specializeGut(new Forest(), 4);

        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(5);
            assertThat(token.getCard().getToughness()).isEqualTo(5);
            assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE, Keyword.HASTE);
        });
    }

    @Test
    void specializedCopiesAreSacrificedAtTheNextEndStep() {
        List<Permanent> tokens = specializeGut(new Forest(), 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(tokens);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(tokens);
    }

    @Test
    void whiteCopiesHaveOneSharedDelayedSacrificeTrigger() {
        List<Permanent> tokens = specializeGut(new Plains(), 0);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(tokens);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(tokens);
    }

    @Test
    void delayedSacrificeCannotSacrificeACopyControlledByAnOpponent() {
        Permanent token = specializeGut(new Forest(), 4).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).singleElement()
                .satisfies(trigger -> assertThat(trigger.getControllerId()).isEqualTo(player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    void optionalFightCanBeDeclinedAndLaterDeathStillExilesTheOpponent() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new GutFanaticalPriestess()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0, List.of(opposingCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        opposingCreature.setMarkedDamage(1);
        harness.runStateBasedActions();

        Permanent gut = findPermanent(player1, "Gut, Fanatical Priestess");
        assertThat(gd.getCardsExiledByPermanent(gut.getId()))
                .extracting(Card::getName).containsExactly("Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void specializationWithoutAnExiledCreatureCreatesNoCopies() {
        Permanent gut = harness.addToBattlefieldAndReturn(player1, new GutFanaticalPriestess());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gut), 4, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(gut);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Gut, Bestial Fanatic");
    }

    @Test
    void specializationCanDiscardAGreenCreatureAndCopiesRetainItsActivatedAbility() {
        Permanent token = specializeGut(new GrizzlyBears(), 4).getFirst();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(manaBefore + 1);
    }

    @Test
    void exileReplacementExpiresAtTheEndOfTheTurn() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new GutFanaticalPriestess()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(opposingCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent gut = findPermanent(player1, "Gut, Fanatical Priestess");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        opposingCreature.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.getCardsExiledByPermanent(gut.getId())).isEmpty();
    }

    @Test
    void specializationRemainsAfterGutDies() {
        specializeGut(new Forest(), 4);
        Permanent gut = findPermanent(player1, "Gut, Bestial Fanatic");

        gut.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Gut, Bestial Fanatic");
        harness.assertInGraveyard(player1, "Gut, Bestial Fanatic");
        harness.assertNotInGraveyard(player1, "Gut, Fanatical Priestess");
    }

    private List<Permanent> specializeGut(Card discard, int abilityIndex) {
        Permanent gut = castGutAndResolveFight();
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int gutIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gut);
        harness.activateAbility(player1, gutIndex, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private Permanent castGutAndResolveFight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new GutFanaticalPriestess()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Gut, Fanatical Priestess");
    }
}
