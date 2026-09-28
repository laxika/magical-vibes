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
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Llanowar Elves");
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

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContainAnyElementsOf(tokens);
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

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gut, Fanatical Priestess"))
                .findFirst()
                .orElseThrow();
    }
}
