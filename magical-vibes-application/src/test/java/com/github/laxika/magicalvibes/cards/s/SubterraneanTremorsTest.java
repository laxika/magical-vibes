package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubterraneanTremors.class, FountainOfYouth.class, GrizzlyBears.class, SerraAngel.class})
class SubterraneanTremorsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to creatures without flying")
    void dealsXDamageToNonFlyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(flyer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("At X=4 destroys all artifacts")
    void destroysArtifactsAtFour() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("At X=8 creates an 8/8 Lizard token")
    void createsLizardTokenAtEight() {
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castAndResolveSorcery(player1, 0, 8);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Lizard");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(8);
    }
}
