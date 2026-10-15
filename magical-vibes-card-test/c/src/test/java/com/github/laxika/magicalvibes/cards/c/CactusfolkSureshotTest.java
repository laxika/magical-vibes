package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.cards.t.ThunderSalvo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CactusfolkSureshot.class, LeatherbackBaloth.class, GrizzlyBears.class, ThunderSalvo.class})
class CactusfolkSureshotTest extends BaseCardTest {

    @Test
    void grantsTrampleAndHasteToOtherOwnCreaturesWithPowerAtLeastFour() {
        Permanent sureshot = addCreatureReady(player1, new CactusfolkSureshot());
        Permanent qualifyingCreature = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent lowPowerCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new LeatherbackBaloth());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, sureshot, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sureshot, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, lowPowerCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lowPowerCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new CactusfolkSureshot());
        Permanent qualifyingCreature = addCreatureReady(player1, new LeatherbackBaloth());

        advanceToCombat(player1);
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.HASTE)).isTrue();

        declareAttackers(List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.HASTE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    void doesNotGrantKeywordsDuringOpponentsCombat() {
        addCreatureReady(player1, new CactusfolkSureshot());
        Permanent other = addCreatureReady(player1, new CactusfolkSureshot());

        advanceToCombat(player2);

        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    void twoSureshotsGrantKeywordsToEachOther() {
        Permanent first = addCreatureReady(player1, new CactusfolkSureshot());
        Permanent second = addCreatureReady(player1, new CactusfolkSureshot());

        advanceToCombat(player1);

        for (Permanent creature : List.of(first, second)) {
            assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    void checksPowerAtResolutionAndKeepsKeywordsAfterPowerDrops() {
        addCreatureReady(player1, new CactusfolkSureshot());
        Permanent other = addCreatureReady(player1, new CactusfolkSureshot());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        resolveAllTriggers();
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isTrue();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainKeywords() {
        addCreatureReady(player1, new CactusfolkSureshot());
        advanceToCombat(player1);

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new CactusfolkSureshot());

        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent sureshot = addCreatureReady(player1, new CactusfolkSureshot());
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, sureshot.getId());
        resolveAllTriggers();

        assertThat(sureshot.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Thunder Salvo");
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        Permanent sureshot = addCreatureReady(player1, new CactusfolkSureshot());
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, sureshot.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(sureshot.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Thunder Salvo");
    }

    @Test
    void wardDoesNotTriggerForControllersSpell() {
        Permanent sureshot = addCreatureReady(player1, new CactusfolkSureshot());
        harness.setHand(player1, List.of(new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, sureshot.getId());
        resolveAllTriggers();

        assertThat(sureshot.getMarkedDamage()).isEqualTo(2);
    }
}
