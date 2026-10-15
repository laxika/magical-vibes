package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({CrewCaptain.class, Murder.class})
class CrewCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Has indestructible during the turn it enters")
    void hasIndestructibleDuringEnteringTurn() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Crew Captain");
        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isTrue();

        captain.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
    }

    @Test
    @DisplayName("Loses indestructible after the turn it enters")
    void losesIndestructibleAfterEnteringTurn() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Crew Captain");
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Can attack on the turn it enters")
    void canAttackOnEnteringTurn() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Crew Captain").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be destroyed on the turn it enters")
    void survivesDestroySpellOnEnteringTurn() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();
        Permanent captain = findPermanent(player1, "Crew Captain");
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, captain.getId());

        harness.assertOnBattlefield(player1, "Crew Captain");
        harness.assertNotInGraveyard(player1, "Crew Captain");
    }

    @Test
    @DisplayName("Can be destroyed on a later turn")
    void diesToDestroySpellOnLaterTurn() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();
        Permanent captain = findPermanent(player1, "Crew Captain");
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, captain.getId());

        harness.assertNotOnBattlefield(player1, "Crew Captain");
        harness.assertInGraveyard(player1, "Crew Captain");
    }

    @Test
    @DisplayName("Another Captain entering does not protect an older Captain")
    void onlyNewCaptainIsIndestructible() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();
        Permanent olderCaptain = findPermanent(player1, "Crew Captain");
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent newCaptain = harness.enterBattlefieldAndReturn(player2, new CrewCaptain());

        assertThat(gqs.hasKeyword(gd, olderCaptain, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newCaptain, Keyword.INDESTRUCTIBLE)).isTrue();
        olderCaptain.setMarkedDamage(2);
        newCaptain.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Crew Captain");
        harness.assertOnBattlefield(player2, "Crew Captain");
    }

    @Test
    @DisplayName("Lethal damage is removed before indestructible expires")
    void survivesCleanupWithLethalDamage() {
        harness.castFromHand(player1, new CrewCaptain(), "{B}{R}{G}");
        harness.passBothPriorities();
        Permanent captain = findPermanent(player1, "Crew Captain");
        captain.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Crew Captain");
        assertThat(captain.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
