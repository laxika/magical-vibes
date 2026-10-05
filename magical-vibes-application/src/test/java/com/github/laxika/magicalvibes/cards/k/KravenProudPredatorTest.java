package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelForge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KravenProudPredator.class, DarksteelForge.class})
class KravenProudPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Power is the greatest mana value among permanents you control")
    void powerUsesGreatestControlledPermanentManaValue() {
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenProudPredator());

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kraven)).isEqualTo(4);

        harness.addToBattlefield(player1, new DarksteelForge());

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, kraven)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent permanents do not affect power")
    void ignoresOpponentPermanents() {
        harness.addToBattlefield(player2, new DarksteelForge());
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenProudPredator());

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power decreases when the highest-mana-value permanent leaves")
    void powerUpdatesWhenHighestManaValuePermanentLeaves() {
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenProudPredator());
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new DarksteelForge());
        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(9);

        gd.playerBattlefields.get(player1.getId()).remove(forge);
        harness.setExile(player1, List.of(forge.getCard()));

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kraven)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power in hand counts controlled permanents but not Kraven itself")
    void powerIsDefinedInHand() {
        KravenProudPredator kraven = new KravenProudPredator();
        harness.setHand(player1, List.of(kraven));

        assertThat(gqs.getEffectiveCardPower(gd, kraven)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, kraven)).isEqualTo(4);

        harness.addToBattlefield(player1, new DarksteelForge());

        assertThat(gqs.getEffectiveCardPower(gd, kraven)).isEqualTo(9);
        assertThat(gqs.getEffectiveCardToughness(gd, kraven)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power in the graveyard uses the owner's controlled permanents")
    void powerIsDefinedInGraveyard() {
        KravenProudPredator kraven = new KravenProudPredator();
        harness.setGraveyard(player1, List.of(kraven));
        harness.addToBattlefield(player2, new DarksteelForge());

        assertThat(gqs.getEffectiveCardPower(gd, kraven)).isZero();

        harness.addToBattlefield(player1, new DarksteelForge());

        assertThat(gqs.getEffectiveCardPower(gd, kraven)).isEqualTo(9);
        assertThat(gqs.getEffectiveCardToughness(gd, kraven)).isEqualTo(4);
    }

    @Test
    @DisplayName("Vigilance lets Kraven attack without tapping")
    void attackingDoesNotTapKraven() {
        Permanent kraven = addCreatureReady(player1, new KravenProudPredator());

        declareAttackers(List.of(0));

        assertThat(kraven.isAttacking()).isTrue();
        assertThat(kraven.isTapped()).isFalse();
    }
}
