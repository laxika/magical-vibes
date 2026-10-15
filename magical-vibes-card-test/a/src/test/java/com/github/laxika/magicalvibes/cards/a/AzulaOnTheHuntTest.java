package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzulaOnTheHunt.class, ErdwalIlluminator.class})
class AzulaOnTheHuntTest extends BaseCardTest {

    @Test
    void attackingAddsManaLosesLifeAndCreatesClue() {
        addCreatureReady(player1, new AzulaOnTheHunt());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void firebendingManaLastsThroughCombatButNotBeyondIt() {
        addCreatureReady(player1, new AzulaOnTheHunt());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void attackAbilitiesUseSeparateStackEntries() {
        addCreatureReady(player1, new AzulaOnTheHunt());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @CardUsed({AzulaOnTheHunt.class, ErdwalIlluminator.class})
    void creatingClueDoesNotInvestigate() {
        addCreatureReady(player1, new AzulaOnTheHunt());
        harness.addToBattlefield(player1, new ErdwalIlluminator());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void firebendingManaCanPayToSacrificeClueAndDraw() {
        addCreatureReady(player1, new AzulaOnTheHunt());
        AzulaOnTheHunt drawnCard = new AzulaOnTheHunt();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));

        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void attackBenefitsAndLifeLossBelongToAzulasController() {
        addCreatureReady(player2, new AzulaOnTheHunt());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
