package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.s.StartFinish;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnointedProcession.class, BladeSplicer.class, SacredCat.class, SongOfTheDryads.class, StartFinish.class})
class AnointedProcessionTest extends BaseCardTest {

    @Test
    @DisplayName("Blade Splicer ETB creates 2 Golem tokens instead of 1 with Anointed Procession")
    void doublesEtbTokenCreation() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Golem");
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("Anointed Procession does not double tokens for the opponent")
    void doesNotDoubleOpponentTokens() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> opponentTokens = findPermanents(player2, "Phyrexian Golem");
        assertThat(opponentTokens).hasSize(1);
    }

    @Test
    @DisplayName("Two Anointed Processions quadruple tokens (1 -> 4)")
    void twoProcessionsQuadrupleTokens() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Golem");
        assertThat(tokens).hasSize(4);
    }

    @Test
    @DisplayName("Without Anointed Procession, Blade Splicer creates exactly 1 token")
    void noDoublingWithoutProcession() {
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Phyrexian Golem");
        assertThat(tokens).hasSize(1);
    }

    @Test
    void doublesEveryTokenInABatch() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.castFromHand(player1, new StartFinish(), "{2}{W}");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior")).hasSize(4);
    }

    @Test
    void doublesEmbalmTokenCopies() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SacredCat()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sacred Cat")).hasSize(2)
                .allSatisfy(p -> assertThat(p.getCard().isToken()).isTrue());
        harness.assertNotInGraveyard(player1, "Sacred Cat");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDoubleWhenProcessionLeavesBeforeTokenCreation() {
        Permanent procession = harness.addToBattlefieldAndReturn(player1, new AnointedProcession());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(procession);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
    }

    @Test
    void doublesWhenProcessionEntersBeforeTokenCreation() {
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new AnointedProcession());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    void doesNotDoubleAfterLosingItsPrintedAbility() {
        Permanent procession = harness.addToBattlefieldAndReturn(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, procession.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new StartFinish(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior")).hasSize(2);
    }
}
