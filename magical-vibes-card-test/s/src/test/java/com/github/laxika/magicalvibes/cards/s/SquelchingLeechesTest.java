package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquelchingLeeches.class, Swamp.class, Forest.class})
class SquelchingLeechesTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals the number of Swamps you control")
    void ptEqualsControlledSwampCount() {
        Permanent leeches = harness.addToBattlefieldAndReturn(player1, new SquelchingLeeches());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(3);
    }

    @Test
    @DisplayName("Is 0/0 with no Swamps")
    void zeroWithoutSwamps() {
        Permanent leeches = harness.addToBattlefieldAndReturn(player1, new SquelchingLeeches());

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only your Swamps")
    void countsOnlyControllersSwamps() {
        Permanent leeches = harness.addToBattlefieldAndReturn(player1, new SquelchingLeeches());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when a Swamp leaves the battlefield")
    void ptUpdatesWhenSwampsChange() {
        Permanent leeches = harness.addToBattlefieldAndReturn(player1, new SquelchingLeeches());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Swamp);

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(0);
    }

    @Test
    void powerAndToughnessIncreaseWhenSwampEnters() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent leeches = harness.addToBattlefieldAndReturn(player1, new SquelchingLeeches());
        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(1);

        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(2);
    }

    @Test
    void usesNewControllersSwampsAfterControlChanges() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent leeches = harness.addToBattlefieldAndReturn(player1, new SquelchingLeeches());
        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(leeches);
        gd.playerBattlefields.get(player2.getId()).add(leeches);

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(2);
    }

    @Test
    void abilityWorksInHandAndGraveyardUsingOwnersSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        SquelchingLeeches leeches = new SquelchingLeeches();
        gd.playerHands.get(player1.getId()).add(leeches);

        assertThat(gqs.getEffectiveCardPower(gd, leeches)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, leeches)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).remove(leeches);
        gd.playerGraveyards.get(player1.getId()).add(leeches);

        assertThat(gqs.getEffectiveCardPower(gd, leeches)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, leeches)).isEqualTo(2);
    }

    @Test
    void diesWithNoSwampsWhenStateBasedActionsRun() {
        harness.addToBattlefield(player1, new SquelchingLeeches());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SquelchingLeeches);
    }
}
