package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterOfWavesTest extends BaseCardTest {

    private List<Permanent> elementalTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental"))
                .toList();
    }

    @Test
    @DisplayName("ETB creates 1/0 Elementals equal to your blue devotion")
    void etbCreatesElementalsEqualToBlueDevotion() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MasterOfWaves()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elementalTokens()).hasSize(3);
        assertThat(elementalTokens()).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Boosts Elemental creatures you control but not other creatures or opponents' Elementals")
    void boostsOnlyYourElementals() {
        harness.addToBattlefield(player1, new MasterOfWaves());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent ownElemental = findPermanent(player1, "Air Elemental");
        Permanent ownNonElemental = findPermanent(player1, "Grizzly Bears");
        Permanent opposingElemental = findPermanent(player2, "Air Elemental");

        assertThat(gqs.getEffectivePower(gd, ownElemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownElemental)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownNonElemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonElemental)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingElemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Protection from red prevents red spells from targeting Master of Waves")
    void protectionFromRedPreventsRedTargeting() {
        Permanent master = harness.addToBattlefieldAndReturn(player2, new MasterOfWaves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, master.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
