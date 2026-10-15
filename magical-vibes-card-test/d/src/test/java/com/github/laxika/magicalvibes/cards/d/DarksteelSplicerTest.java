package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelSplicer.class, CrawlingChorus.class, GrizzlyBears.class, MaskwoodNexus.class})
class DarksteelSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and creates one Phyrexian Golem per opponent")
    void entersAndCreatesGolemPerOpponent() {
        castDarksteelSplicer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Another nontoken Phyrexian entering creates a Golem")
    void anotherNontokenPhyrexianCreatesGolem() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        harness.castFromHand(player1, new CrawlingChorus(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Phyrexian creature does not create a Golem")
    void nonPhyrexianDoesNotCreateGolem() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(golemCount()).isZero();
    }

    @Test
    @DisplayName("Golems you control have indestructible")
    void golemsHaveIndestructible() {
        castDarksteelSplicer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Phyrexian Golem"), Keyword.INDESTRUCTIBLE))
                .isTrue();
    }

    @Test
    @DisplayName("An opposing Phyrexian does not trigger your Splicer or receive indestructible")
    void opposingPhyrexianDoesNotTriggerOrReceiveIndestructible() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        harness.enterBattlefieldAndReturn(player2, new DarksteelSplicer());
        harness.passBothPriorities();

        assertThat(golemCount()).isZero();
        var opposingGolem = findPermanent(player2, "Phyrexian Golem");
        assertThat(opposingGolem).isNotNull();
        assertThat(gqs.hasKeyword(gd, opposingGolem, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, findPermanent(player2, "Darksteel Splicer")));
        assertThat(gqs.hasKeyword(gd, opposingGolem, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Two Splicers each trigger once and their Phyrexian tokens do not trigger again")
    void twoSplicersEachTriggerOnceWithoutTokenRecursion() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        castDarksteelSplicer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Golems resist destruction only while the Splicer remains")
    void indestructibleEndsWhenSplicerLeaves() {
        castDarksteelSplicer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        var golem = findPermanent(player1, "Phyrexian Golem");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .tryDestroyPermanent(gd, golem));
        harness.assertOnBattlefield(player1, "Phyrexian Golem");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Darksteel Splicer"), Keyword.INDESTRUCTIBLE))
                .isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, findPermanent(player1, "Darksteel Splicer")));
        assertThat(gqs.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .tryDestroyPermanent(gd, golem));
        harness.assertNotOnBattlefield(player1, "Phyrexian Golem");
    }

    @Test
    @DisplayName("The creation trigger resolves even if the Splicer leaves first")
    void creationTriggerSurvivesSourceLeaving() {
        var splicer = harness.enterBattlefieldAndReturn(player1, new DarksteelSplicer());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, splicer));
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Phyrexian Golem"), Keyword.INDESTRUCTIBLE))
                .isFalse();
    }

    @Test
    @DisplayName("A creature made Phyrexian by Maskwood Nexus triggers the Splicer")
    @CardUsed({DarksteelSplicer.class, MaskwoodNexus.class, GrizzlyBears.class})
    void grantedPhyrexianTypeTriggersSplicer() {
        harness.addToBattlefield(player1, new DarksteelSplicer());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golemCount()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Splicer that is itself a Golem gains indestructible")
    @CardUsed({DarksteelSplicer.class, MaskwoodNexus.class})
    void splicerMadeIntoGolemHasIndestructible() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        var splicer = harness.addToBattlefieldAndReturn(player1, new DarksteelSplicer());

        assertThat(gqs.hasEffectiveSubtype(gd, splicer, CardSubtype.GOLEM)).isTrue();
        assertThat(gqs.hasKeyword(gd, splicer, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .tryDestroyPermanent(gd, splicer));
        harness.assertOnBattlefield(player1, "Darksteel Splicer");
    }

    private void castDarksteelSplicer() {
        harness.castFromHand(player1, new DarksteelSplicer(), "{6}{W}");
    }

    private long golemCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .count();
    }
}
