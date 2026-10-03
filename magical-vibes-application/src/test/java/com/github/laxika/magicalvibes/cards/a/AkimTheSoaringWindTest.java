package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.r.RiteOfReplication;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkimTheSoaringWind.class, GrizzlyBears.class, RaiseTheAlarm.class,
        Panharmonicon.class, RiteOfReplication.class})
class AkimTheSoaringWindTest extends BaseCardTest {

    @Test
    @DisplayName("Creating tokens for the first time each turn creates a flying Bird")
    void createsBirdOncePerTurn() {
        addCreatureReady(player1, new AkimTheSoaringWind());
        castRaiseTheAlarm();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        assertThat(findPermanents(player1, "Bird").getFirst().getCard().hasKeyword(Keyword.FLYING)).isTrue();

        castRaiseTheAlarm();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability grants double strike to creature tokens only")
    void grantsDoubleStrikeToCreatureTokens() {
        Permanent akim = addCreatureReady(player1, new AkimTheSoaringWind());
        Permanent nonToken = addCreatureReady(player1, new GrizzlyBears());
        castRaiseTheAlarm();
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(akim), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonToken, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void castRaiseTheAlarm() {
        harness.castFromHand(player1, new RaiseTheAlarm(), "{1}{W}");
        resolveAllTriggers();
    }

    @Test
    void doesNotTriggerIfTokensWereCreatedBeforeAkimEntered() {
        castRaiseTheAlarm();
        harness.enterBattlefieldAndReturn(player1, new AkimTheSoaringWind());

        castRaiseTheAlarm();

        assertThat(findPermanents(player1, "Soldier")).hasSize(4);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    void tokenCreationTriggerIsNotDoubledByPanharmonicon() {
        addCreatureReady(player1, new AkimTheSoaringWind());
        harness.addToBattlefield(player1, new Panharmonicon());

        castRaiseTheAlarm();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }

    @Test
    void tokenCopyOfAkimGivesItselfDoubleStrike() {
        Permanent opponentAkim = addCreatureReady(player2, new AkimTheSoaringWind());
        harness.setHand(player1, List.of(new RiteOfReplication()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, opponentAkim.getId());
        resolveAllTriggers();
        Permanent tokenAkim = findPermanent(player1, "Akim, the Soaring Wind");

        activateDoubleStrike(tokenAkim);

        assertThat(gqs.hasKeyword(gd, tokenAkim, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentAkim, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void opponentsTokenCreationDoesNotTriggerOrConsumeFirstCreation() {
        addCreatureReady(player1, new AkimTheSoaringWind());
        harness.castFromHand(player2, new RaiseTheAlarm(), "{1}{W}");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bird")).isEmpty();

        castRaiseTheAlarm();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }

    @Test
    void laterTokensDoNotGainDoubleStrikeFromResolvedAbility() {
        Permanent akim = addCreatureReady(player1, new AkimTheSoaringWind());
        castRaiseTheAlarm();
        List<Permanent> originalSoldiers = findPermanents(player1, "Soldier");

        activateDoubleStrike(akim);
        castRaiseTheAlarm();

        assertThat(originalSoldiers).allSatisfy(soldier ->
                assertThat(gqs.hasKeyword(gd, soldier, Keyword.DOUBLE_STRIKE)).isTrue());
        assertThat(findPermanents(player1, "Soldier").subList(2, 4)).allSatisfy(soldier ->
                assertThat(gqs.hasKeyword(gd, soldier, Keyword.DOUBLE_STRIKE)).isFalse());
    }

    private void activateDoubleStrike(Permanent akim) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(akim), null, null);
        resolveAllTriggers();
    }
}
