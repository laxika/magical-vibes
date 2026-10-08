package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnubhornSentry.class, Forest.class, GrizzlyBears.class})
class SnubhornSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 with the city's blessing")
    void getsPowerBonusWithCityBlessing() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SnubhornSentry());
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ascend grants the city's blessing when the tenth permanent enters")
    void ascendsWhenTenthPermanentEnters() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SnubhornSentry());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, sentry)).isZero();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sentry counts itself when entering as the tenth permanent")
    void ascendsOnItsOwnEntry() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.castFromHand(player1, new SnubhornSentry(), "{W}");
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.passBothPriorities();

        Permanent sentry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof SnubhornSentry)
                .findFirst().orElseThrow();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Forest);
        harness.runStateBasedActions();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent permanents and blessing do not empower Sentry")
    void onlyItsControllersPermanentsAndBlessingCount() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        gd.playersWithCityBlessing.add(player2.getId());

        Permanent sentry = harness.enterBattlefieldAndReturn(player1, new SnubhornSentry());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, sentry)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }
}
