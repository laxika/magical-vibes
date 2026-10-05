package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalonianTwingrove.class, Forest.class, Plains.class})
class KalonianTwingroveTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equal the number of Forests you control, and a token copy of that CDA enters")
    void ptAndTokenScaleWithForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        castTwingrove();

        Permanent twingrove = findByName("Kalonian Twingrove");
        assertThat(gqs.getEffectivePower(gd, twingrove)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, twingrove)).isEqualTo(3);

        Permanent token = findToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Both P/T update as Forests come and go")
    void ptUpdatesDynamically() {
        harness.addToBattlefield(player1, new Forest());

        castTwingrove();

        Permanent twingrove = findByName("Kalonian Twingrove");
        Permanent token = findToken();
        assertThat(gqs.getEffectivePower(gd, twingrove)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, twingrove)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("With no Forests both are 0/0 and die to state-based actions")
    void diesWithoutForests() {
        castTwingrove();

        harness.assertNotOnBattlefield(player1, "Kalonian Twingrove");
        harness.assertInGraveyard(player1, "Kalonian Twingrove");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    private void castTwingrove() {
        harness.castFromHand(player1, new KalonianTwingrove(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The enter trigger survives the source dying and counts Forests when its token enters")
    void tokenEntersAfterSourceDies() {
        harness.castFromHand(player1, new KalonianTwingrove(), "{5}{G}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Kalonian Twingrove");
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kalonian Twingrove");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
        Permanent token = findToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("The characteristic ability works in hand and graveyard using the owner's Forests")
    void characteristicAbilityWorksOutsideBattlefield() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        KalonianTwingrove card = new KalonianTwingrove();
        harness.setHand(player1, List.of(card));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card));
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);
    }

    private Permanent findByName(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    private Permanent findToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
