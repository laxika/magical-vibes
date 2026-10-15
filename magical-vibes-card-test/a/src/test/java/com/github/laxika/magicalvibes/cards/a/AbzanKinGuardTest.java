package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MasteryOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.ScouredBarrens;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbzanKinGuard.class, AvenSkirmisher.class, AleshasVanguard.class,
        ArchersOfQarsi.class, MasteryOfTheUnseen.class, ScouredBarrens.class})
class AbzanKinGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink while controller controls a white permanent")
    void hasLifelinkWithWhitePermanent() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, new AvenSkirmisher());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Has lifelink while controller controls a black permanent")
    void hasLifelinkWithBlackPermanent() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, new AleshasVanguard());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not have lifelink without a white or black permanent")
    void noLifelinkWithoutMatchingPermanent() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, new ArchersOfQarsi());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's white or black permanent does not grant lifelink")
    void opponentMatchingPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        harness.addToBattlefield(player2, new AvenSkirmisher());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Abzan Kin-Guard"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Loses lifelink when the matching permanent leaves the battlefield")
    void losesLifelinkWhenMatchingPermanentLeaves() {
        harness.addToBattlefield(player1, new AbzanKinGuard());
        Permanent matchingPermanent = harness.addToBattlefieldAndReturn(player1, new AleshasVanguard());
        Permanent kinGuard = findPermanent(player1, "Abzan Kin-Guard");

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(matchingPermanent);

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void gainsLifelinkWhenWhiteEnchantmentEnters() {
        Permanent kinGuard = harness.addToBattlefieldAndReturn(player1, new AbzanKinGuard());
        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player1, new MasteryOfTheUnseen());

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void whiteAndBlackManaAbilitiesDoNotMakeALandColored() {
        Permanent kinGuard = harness.addToBattlefieldAndReturn(player1, new AbzanKinGuard());
        harness.addToBattlefield(player1, new ScouredBarrens());

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void remainingMatchingPermanentKeepsLifelink() {
        Permanent kinGuard = harness.addToBattlefieldAndReturn(player1, new AbzanKinGuard());
        Permanent white = harness.addToBattlefieldAndReturn(player1, new AvenSkirmisher());
        harness.addToBattlefield(player1, new AleshasVanguard());

        gd.playerBattlefields.get(player1.getId()).remove(white);

        assertThat(gqs.hasKeyword(gd, kinGuard, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void lifelinkGainsLifeFromCombatDamageOnlyForKinGuard() {
        addCreatureReady(player1, new AbzanKinGuard());
        Permanent white = addCreatureReady(player1, new AvenSkirmisher());
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isFalse();

        declareAttackers(List.of(0, 1));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 16);
    }

    @Test
    void combatDamageDoesNotGainLifeWithoutMatchingPermanent() {
        addCreatureReady(player1, new AbzanKinGuard());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }
}
