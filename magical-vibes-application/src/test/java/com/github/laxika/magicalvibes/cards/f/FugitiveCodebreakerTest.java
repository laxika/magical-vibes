package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ExtractAConfession;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FugitiveCodebreaker.class, ExtractAConfession.class, Island.class, Shock.class})
class FugitiveCodebreakerTest extends BaseCardTest {

    @Test
    void disguiseCostIsReducedByInstantAndSorceryCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new FugitiveCodebreaker()));
        FugitiveCodebreaker card = new FugitiveCodebreaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent codebreaker = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(codebreaker));

        assertThat(codebreaker.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void turningFaceUpDiscardsHandThenDrawsThreeCards() {
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        Island thirdDraw = new Island();
        FugitiveCodebreaker discarded = new FugitiveCodebreaker();
        FugitiveCodebreaker card = new FugitiveCodebreaker();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(card, discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent codebreaker = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(codebreaker));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(discarded);
        assertThat(codebreaker.isFaceDown()).isFalse();
    }

    @Test
    void prowessResolvesBeforeTheSpellAndStacksForMultipleSpells() {
        Permanent codebreaker = addCreatureReady(player1, new FugitiveCodebreaker());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, codebreaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, codebreaker)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, codebreaker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, codebreaker)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, codebreaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, codebreaker)).isEqualTo(1);
    }

    @Test
    void prowessDoesNotTriggerForOpponentsSpellsOrCreatureSpells() {
        Permanent codebreaker = addCreatureReady(player1, new FugitiveCodebreaker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gqs.getEffectivePower(gd, codebreaker)).isEqualTo(2);

        harness.castFromHand(player1, new FugitiveCodebreaker(), "{1}{R}");
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, codebreaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, codebreaker)).isEqualTo(1);
    }

    @Test
    void faceDownCreatureHasNoProwessAndHasWard() {
        harness.setHand(player1, List.of(new FugitiveCodebreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent codebreaker = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, codebreaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, codebreaker)).isEqualTo(2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, codebreaker.getId());
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(codebreaker);
        assertThat(codebreaker.isFaceDown()).isTrue();
    }

    @Test
    void reductionCountsSorceriesButNotOpponentsGraveyardAndCannotReduceRedCost() {
        harness.setGraveyard(player1, List.of(new Shock(), new ExtractAConfession(),
                new Shock(), new ExtractAConfession(), new Shock(), new ExtractAConfession()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new FugitiveCodebreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent codebreaker = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(codebreaker.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, 0);
        assertThat(codebreaker.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsGraveyardDoesNotReduceDisguiseCost() {
        harness.setGraveyard(player2, List.of(new Shock(), new ExtractAConfession()));
        harness.setHand(player1, List.of(new FugitiveCodebreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isFaceDown()).isTrue();
    }

    @Test
    void emptyHandStillDrawsThreeAndTriggerWaitsForResolution() {
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new FugitiveCodebreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void normalCastDoesNotDiscardHandAndCanAttackImmediately() {
        FugitiveCodebreaker card = new FugitiveCodebreaker();
        Island retained = new Island();
        harness.setHand(player1, List.of(card, retained));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        Permanent codebreaker = findPermanentForCard(card);
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(codebreaker.isAttacking()).isTrue();
    }

    private Permanent findPermanentForCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
