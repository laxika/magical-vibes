package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostflameSliver.class, GemhideSliver.class, AshcoatBear.class})
class GhostflameSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all Slivers colorless, including opposing Slivers")
    void makesAllSliversColorless() {
        Permanent ghostflameSliver = harness.addToBattlefieldAndReturn(player1, new GhostflameSliver());
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new GemhideSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new GemhideSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

        assertThat(gqs.getEffectiveColors(gd, ghostflameSliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, ownSliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opposingSliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, nonSliver)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Slivers regain their colors when the last Ghostflame Sliver leaves")
    void colorsReturnAfterLastSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GhostflameSliver());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GhostflameSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player2, new GemhideSliver());

        first.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Ghostflame Sliver");
        assertThat(gqs.getEffectiveColors(gd, sliver)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, second)).isEmpty();

        second.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Ghostflame Sliver");
        assertThat(gqs.getEffectiveColors(gd, sliver)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Sliver cards in hand and graveyard retain their colors")
    void doesNotChangeColorsOutsideBattlefield() {
        harness.addToBattlefield(player1, new GhostflameSliver());
        GhostflameSliver inHand = new GhostflameSliver();
        GhostflameSliver inGraveyard = new GhostflameSliver();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player2, List.of(inGraveyard));

        assertThat(gqs.getEffectiveCardColors(gd, inHand))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(gqs.getEffectiveCardColors(gd, inGraveyard))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
    }

    @Test
    @DisplayName("A Sliver spell retains its colors until it enters the battlefield")
    void spellBecomesColorlessOnlyOnBattlefield() {
        harness.addToBattlefield(player2, new GhostflameSliver());
        GhostflameSliver spell = new GhostflameSliver();

        harness.castFromHand(player1, spell, "{B}{R}");

        assertThat(gqs.getEffectiveCardColors(gd, spell))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghostflame Sliver");
        Permanent resolved = findPermanent(player1, "Ghostflame Sliver");
        assertThat(gqs.getEffectiveColors(gd, resolved)).isEmpty();
    }
}
