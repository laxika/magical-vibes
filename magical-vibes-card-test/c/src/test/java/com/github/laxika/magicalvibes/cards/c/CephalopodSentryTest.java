package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalopodSentry.class, PropheticPrism.class, CrawlingChorus.class})
class CephalopodSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts you control; toughness stays 5")
    void powerEqualsControlledArtifacts() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new CephalopodSentry());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts only artifacts controlled by the sentry's controller")
    void countsOnlyControllersArtifacts() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new CephalopodSentry());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new PropheticPrism());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power updates when controlled artifacts enter and leave the battlefield")
    void powerUpdatesWhenArtifactsChange() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new CephalopodSentry());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);

        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof PropheticPrism);
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonartifact creatures and artifacts outside the battlefield do not increase power")
    void ignoresNonartifactsAndCardsInOtherZones() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new CephalopodSentry());
        harness.addToBattlefield(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.setGraveyard(player1, List.of(new PropheticPrism()));
        harness.setExile(player1, List.of(new PropheticPrism()));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power is defined in hand and graveyard and can be zero")
    void powerIsDefinedOutsideBattlefield() {
        CephalopodSentry inHand = new CephalopodSentry();
        CephalopodSentry inGraveyard = new CephalopodSentry();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player2, new PropheticPrism());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new PropheticPrism());
        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(5);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(5);
    }
}
