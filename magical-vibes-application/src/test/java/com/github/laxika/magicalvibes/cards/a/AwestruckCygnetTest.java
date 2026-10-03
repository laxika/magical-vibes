package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PsychicPaper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwestruckCygnet.class, AceFlockbringer.class, PsychicPaper.class})
class AwestruckCygnetTest extends BaseCardTest {

    @Test
    @DisplayName("Starts as a 2/1 without flying, vigilance, or the transformed name")
    void startsUntransformed() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());

        assertThat(gqs.getEffectivePower(gd, cygnet)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cygnet)).isEqualTo(1);
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Awestruck Cygnet");
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Each untransformed copy triggers and pending triggers stop after transformation")
    void eachCopyTriggersAndPendingTriggersStopAfterTransformation() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());

        castFlyingCreature(player1);

        assertThat(gd.getCardIntensity(first.getCard().getId())).isEqualTo(2);
        assertThat(gd.getCardIntensity(second.getCard().getId())).isEqualTo(2);
        castFlyingCreature(player1);

        assertThat(gd.getCardIntensity(first.getCard().getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(second.getCard().getId())).isEqualTo(3);
        assertTransformed(first);
        assertTransformed(second);
    }

    @Test
    @DisplayName("Does not intensify an opponent's copy")
    void doesNotIntensifyOpponentsCopy() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AwestruckCygnet());

        castFlyingCreature(player1);

        assertThat(gd.getCardIntensity(own.getCard().getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(opponent.getCard().getId())).isZero();
    }

    @Test
    void threeFlyingEntriesTransformAndFurtherEntriesDoNotTrigger() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());

        castFlyingCreature(player1);
        castFlyingCreature(player1);
        assertThat(gd.getCardIntensity(cygnet.getCard().getId())).isEqualTo(2);
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Awestruck Cygnet");
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.FLYING)).isFalse();

        castFlyingCreature(player1);
        assertTransformed(cygnet);
        castFlyingCreature(player1);
        assertThat(gd.getCardIntensity(cygnet.getCard().getId())).isEqualTo(3);
    }

    @Test
    void nonFlyingCreatureAndOpponentsFlyingCreatureDoNotTrigger() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());

        harness.enterBattlefieldAndReturn(player1, new AwestruckCygnet());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new AceFlockbringer());
        resolveAllTriggers();

        assertThat(gd.getCardIntensity(cygnet.getCard().getId())).isZero();
    }

    @Test
    void intensifiesOwnedCardsInOtherZonesAndIntensitySurvivesEntering() {
        harness.addToBattlefield(player1, new AwestruckCygnet());
        AwestruckCygnet hand = new AwestruckCygnet();
        AwestruckCygnet library = new AwestruckCygnet();
        AwestruckCygnet graveyard = new AwestruckCygnet();
        AwestruckCygnet exile = new AwestruckCygnet();
        harness.setHand(player1, List.of(hand));
        harness.setLibrary(player1, List.of(library));
        harness.setGraveyard(player1, List.of(graveyard));
        harness.setExile(player1, List.of(exile));

        for (int i = 0; i < 3; i++) {
            harness.enterBattlefieldAndReturn(player1, new AceFlockbringer());
            resolveAllTriggers();
        }

        for (AwestruckCygnet card : List.of(hand, library, graveyard, exile)) {
            assertThat(gd.getCardIntensity(card.getId())).isEqualTo(3);
        }
        harness.setHand(player1, List.of());
        Permanent swan = harness.enterBattlefieldAndReturn(player1, hand);
        resolveAllTriggers();
        assertTransformed(swan);
        assertThat(gd.getCardIntensity(hand.getId())).isEqualTo(3);
    }

    @Test
    void enteringRadiantSwanTriggersAnotherCygnetButDoesNotIntensifyItself() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        AwestruckCygnet transformed = new AwestruckCygnet();
        gd.intensifyCard(transformed, 3);

        Permanent swan = harness.enterBattlefieldAndReturn(player1, transformed);
        resolveAllTriggers();

        assertTransformed(swan);
        assertThat(gd.getCardIntensity(cygnet.getCard().getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(transformed.getId())).isEqualTo(3);
    }

    @Test
    void renamedCygnetDoesNotTrigger() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        Permanent paper = harness.addToBattlefieldAndReturn(player1, new PsychicPaper());
        paper.setAttachedTo(cygnet.getId());
        paper.setChosenName("Ace Flockbringer");
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Ace Flockbringer");

        harness.enterBattlefieldAndReturn(player1, new AceFlockbringer());

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(gd.getCardIntensity(cygnet.getCard().getId())).isZero();
    }

    @Test
    void rechecksNameWhenPendingTriggerResolves() {
        Permanent cygnet = harness.addToBattlefieldAndReturn(player1, new AwestruckCygnet());
        Permanent paper = harness.addToBattlefieldAndReturn(player1, new PsychicPaper());
        harness.enterBattlefieldAndReturn(player1, new AceFlockbringer());
        assertThat(gd.stack).hasSize(1);

        paper.setAttachedTo(cygnet.getId());
        paper.setChosenName("Ace Flockbringer");
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Ace Flockbringer");
        resolveAllTriggers();

        assertThat(gd.getCardIntensity(cygnet.getCard().getId())).isZero();
    }

    private void assertTransformed(Permanent cygnet) {
        assertThat(gqs.getEffectivePower(gd, cygnet)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cygnet)).isEqualTo(4);
        assertThat(gqs.getEffectiveName(gd, cygnet)).isEqualTo("Radiant Swan");
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, cygnet, Keyword.VIGILANCE)).isTrue();
    }

    private void castFlyingCreature(Player player) {
        harness.castFromHand(player, new AceFlockbringer(), "{1}{W}{U}");
        resolveAllTriggers();
    }
}
