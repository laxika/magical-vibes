package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.q.QueensCommission;
import com.github.laxika.magicalvibes.cards.s.ShootTheSheriff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KambalProfiteeringMayor.class, QueensCommission.class, ShootTheSheriff.class})
class KambalProfiteeringMayorTest extends BaseCardTest {

    @Test
    void tokensYouControlMakeOpponentsLoseLifeAndYouGainLife() {
        addCreatureReady(player1, new KambalProfiteeringMayor());

        castQueensCommission(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(vampires(player1)).hasSize(2);
    }

    @Test
    void opponentsTokensCreateTappedCopies() {
        addCreatureReady(player1, new KambalProfiteeringMayor());

        castQueensCommission(player2);

        assertThat(vampires(player1)).hasSize(2).allSatisfy(token -> assertThat(token.isTapped()).isTrue());
        assertThat(vampires(player2)).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void opponentTokenCopyTriggerFiresOnlyOnceEachTurn() {
        addCreatureReady(player1, new KambalProfiteeringMayor());
        harness.setHand(player2, List.of(new QueensCommission(), new QueensCommission()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        castQueensCommissionFromHand(player2);
        castQueensCommissionFromHand(player2);

        assertThat(vampires(player1)).hasSize(2);
    }

    @Test
    void ownTokenTriggerIsNotLimitedToOnceEachTurn() {
        addCreatureReady(player1, new KambalProfiteeringMayor());

        castQueensCommission(player1);
        castQueensCommission(player1);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(vampires(player1)).hasSize(4);
        assertThat(vampires(player2)).isEmpty();
    }

    @Test
    void copiesTokenThatLeavesBeforeCopyTriggerResolves() {
        addCreatureReady(player1, new KambalProfiteeringMayor());
        harness.setHand(player2, List.of(new QueensCommission()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(vampires(player2)).hasSize(2);
        assertThat(vampires(player1)).isEmpty();

        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, vampires(player2).getFirst().getId());
        resolveAllTriggers();

        assertThat(vampires(player2)).hasSize(1);
        assertThat(vampires(player1)).hasSize(2)
                .allSatisfy(token -> assertThat(token.isTapped()).isTrue());
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void copyTriggerStillResolvesAfterKambalLeaves() {
        Permanent kambal = addCreatureReady(player1, new KambalProfiteeringMayor());
        harness.setHand(player2, List.of(new QueensCommission(), new ShootTheSheriff()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(vampires(player2)).hasSize(2);
        assertThat(vampires(player1)).isEmpty();

        harness.castInstant(player2, 0, kambal.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kambal, Profiteering Mayor");
        assertThat(vampires(player1)).hasSize(2)
                .allSatisfy(token -> assertThat(token.isTapped()).isTrue());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castQueensCommission(Player player) {
        harness.setHand(player, List.of(new QueensCommission()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        castQueensCommissionFromHand(player);
    }

    private void castQueensCommissionFromHand(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player, 0, 0);
        resolveAllTriggers();
    }

    private List<Permanent> vampires(Player player) {
        return findPermanents(player, "Vampire");
    }
}
