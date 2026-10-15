package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BanishingStroke;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({AvacynAngelOfHope.class, GrizzlyBears.class, Forest.class, WrathOfGod.class, BanishingStroke.class})
class AvacynAngelOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Other permanents you control have indestructible, creatures and noncreatures alike")
    void grantsIndestructibleToOtherOwnPermanents() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect permanents an opponent controls")
    void doesNotAffectOpponentPermanents() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        Permanent forest = findPermanent(player2, "Forest");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Your creatures survive a board wipe while an opponent's do not")
    void protectedCreaturesSurviveWrath() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Avacyn, Angel of Hope");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Permanents entering after Avacyn immediately receive indestructible")
    void protectsNewlyEnteredPermanents() {
        harness.addToBattlefield(player1, new AvacynAngelOfHope());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Lethal damage remains marked and kills a protected creature when Avacyn leaves")
    void lethalDamageKillsCreatureAfterProtectionEnds() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new AvacynAngelOfHope());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        avacyn.setMarkedDamage(8);
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Avacyn, Angel of Hope");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(2);

        harness.setHand(player2, List.of(new BanishingStroke()));
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.castAndResolveInstant(player2, 0, avacyn.getId());

        harness.assertNotOnBattlefield(player1, "Avacyn, Angel of Hope");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing Avacyn without destroying her ends protection for all other permanents")
    void protectionEndsWhenAvacynLeaves() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new AvacynAngelOfHope());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player2, List.of(new BanishingStroke()));
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.castAndResolveInstant(player2, 0, avacyn.getId());

        harness.assertNotOnBattlefield(player1, "Avacyn, Angel of Hope");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(avacyn.getCard().getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
