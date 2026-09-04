package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.t.TheWaterCrystal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfBlood.class, PhyrexianWalker.class})
class SongOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards from library")
    void millsFourCards() {
        harness.setLibrary(player1, List.of(
                new PhyrexianWalker(), new SongOfBlood(), new PhyrexianWalker(), new SongOfBlood(),
                new SongOfBlood()));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5); // 4 milled + Song of Blood
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(PhyrexianWalker.class::isInstance)
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mills only cards available when library has fewer than four")
    void millsOnlyAvailableCards() {
        harness.setLibrary(player1, List.of(new PhyrexianWalker(), new SongOfBlood()));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3); // 2 milled + Song of Blood
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(PhyrexianWalker.class::isInstance)
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking creature gets +1/+0 per creature card milled")
    void attackerBoostedPerMilledCreature() {
        harness.setLibrary(player1, List.of(
                new PhyrexianWalker(), new SongOfBlood(), new PhyrexianWalker(), new SongOfBlood()));

        Permanent attacker = addCreatureReady(player1, new PhyrexianWalker());

        castAndResolve();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking creature gets its own boost")
    void eachAttackingCreatureIsBoosted() {
        harness.setLibrary(player1, List.of(
                new PhyrexianWalker(), new SongOfBlood(), new PhyrexianWalker(), new SongOfBlood()));

        Permanent firstAttacker = addCreatureReady(player1, new PhyrexianWalker());
        Permanent secondAttacker = addCreatureReady(player1, new PhyrexianWalker());

        castAndResolve();

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(firstAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(secondAttacker.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost when no creature cards are milled")
    void noBoostWithoutMilledCreatures() {
        harness.setLibrary(player1, List.of(
                new SongOfBlood(), new SongOfBlood(), new SongOfBlood(), new SongOfBlood()));

        Permanent attacker = addCreatureReady(player1, new PhyrexianWalker());

        castAndResolve();

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Opponent's attacker also gets the boost")
    void boostsAnyCreatureThatAttacks() {
        harness.setLibrary(player1, List.of(
                new PhyrexianWalker(), new SongOfBlood(), new SongOfBlood(), new SongOfBlood()));

        Permanent oppAttacker = addCreatureReady(player2, new PhyrexianWalker());

        castAndResolve();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(oppAttacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @CardUsed(TheWaterCrystal.class)
    @DisplayName("Counts every card added to the mill event by a replacement effect")
    void countsCardsAddedByMillReplacement() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.setLibrary(player2, cards(8));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new SongOfBlood(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(9); // 8 milled + Song of Blood

        Permanent attacker = addCreatureReady(player2, new PhyrexianWalker());
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("Attacker boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        harness.setLibrary(player1, List.of(
                new PhyrexianWalker(), new SongOfBlood(), new SongOfBlood(), new SongOfBlood()));

        Permanent attacker = addCreatureReady(player1, new PhyrexianWalker());

        castAndResolve();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new SongOfBlood(), "{1}{R}");
        harness.passBothPriorities();
    }

    private List<Card> cards(int count) {
        return IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new PhyrexianWalker())
                .toList();
    }
}
