package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ActOfAggression;
import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianIngester.class, GrizzlyBears.class, PhyrexianDigester.class,
        PsychosisCrawler.class, Dismember.class, ActOfAggression.class})
class PhyrexianIngesterTest extends BaseCardTest {

    private void castIngesterAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PhyrexianIngester(), "{6}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("ETB exiles target nontoken creature and imprints it")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Gets +X/+Y equal to exiled creature's power and toughness")
    void boostsFromImprintedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");

        // Base 3/3 + Grizzly Bears 2/2 = 5/5
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gets correct boost with asymmetric P/T creature")
    void boostsFromAsymmetricCreature() {
        harness.addToBattlefield(player2, new PhyrexianDigester()); // 2/1
        UUID digesterId = harness.getPermanentId(player2, "Phyrexian Digester");

        castIngesterAndAcceptMay(digesterId);

        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");

        // Base 3/3 + Phyrexian Digester 2/1 = 5/4
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(4);
    }

    @Test
    @DisplayName("No P/T boost when may ability is declined")
    void noBoostWhenDeclined() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PhyrexianIngester(), "{6}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");

        // Base 3/3, no boost
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(3);

        // Grizzly Bears still on battlefield
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can exile own creature")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exiled creature stays exiled when Phyrexian Ingester leaves the battlefield")
    void exiledCreatureStaysExiledWhenIngesterLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castIngesterAndAcceptMay(bearsId);

        UUID ingesterId = harness.getPermanentId(player1, "Phyrexian Ingester");
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, ingesterId);

        harness.assertInGraveyard(player1, "Phyrexian Ingester");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Bonus tracks characteristic-defining power and toughness in exile")
    void tracksExiledCreaturesChangingCharacteristics() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new PsychosisCrawler());
        UUID crawlerId = harness.getPermanentId(player2, "Psychosis Crawler");

        castIngesterAndAcceptMay(crawlerId);

        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(5);

        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(6);
    }

    @Test
    @DisplayName("Changing control before the trigger resolves preserves the linked bonus")
    void imprintsWhenControlChangesBeforeResolution() {
        harness.addToBattlefield(player2, new PhyrexianDigester());
        UUID digesterId = harness.getPermanentId(player2, "Phyrexian Digester");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PhyrexianIngester(), "{6}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, digesterId);

        UUID ingesterId = harness.getPermanentId(player1, "Phyrexian Ingester");
        harness.setHand(player2, List.of(new ActOfAggression()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, ingesterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Phyrexian Digester");
        Permanent ingester = findPermanent(player2, "Phyrexian Ingester");
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(4);
    }

    @Test
    @CardUsed({Panharmonicon.class})
    @DisplayName("Multiple imprint triggers sum the characteristics of all exiled creature cards")
    void sumsBonusesFromMultipleExiledCards() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new PhyrexianDigester());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PhyrexianDigester());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PhyrexianIngester(), "{6}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Phyrexian Digester"))
                .hasSize(2);
        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(5);
    }

    @Test
    @DisplayName("An illegal target leaves Ingester without a bonus")
    void targetRemovedBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new PhyrexianDigester());
        UUID digesterId = harness.getPermanentId(player2, "Phyrexian Digester");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PhyrexianIngester(), "{6}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, digesterId);

        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, digesterId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Digester");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        Permanent ingester = findPermanent(player1, "Phyrexian Ingester");
        assertThat(gqs.getEffectivePower(gd, ingester)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ingester)).isEqualTo(3);
    }
}
