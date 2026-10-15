package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

        Permanent token = findPermanent(player1, "Lizard");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Lizard");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(8);
    }

    @Test
    @DisplayName("X=0 leaves creatures and artifacts untouched and creates no token")
    void zeroLeavesBattlefieldUntouched() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(countPermanents(player1, "Lizard")).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("X=1 damages creatures controlled by both players without damaging players")
    void damagesBothPlayersCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingBear.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("X=3 does not destroy artifacts")
    void preservesArtifactsBelowFour() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(countPermanents(player1, "Lizard")).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 7})
    @DisplayName("X below eight destroys artifacts but does not create a Lizard")
    void destroysArtifactsWithoutCreatingToken(int x) {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, x + 1);

        harness.castAndResolveSorcery(player1, 0, x);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(countPermanents(player1, "Lizard")).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9})
    @DisplayName("X at least eight applies all three effects and leaves the new Lizard undamaged")
    void appliesAllEffectsBeforeCreatingToken(int x) {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new SubterraneanTremors()));
        harness.addMana(player1, ManaColor.RED, x + 1);

        harness.castAndResolveSorcery(player1, 0, x);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(countPermanents(player1, "Lizard")).isEqualTo(1);
        assertThat(countPermanents(player2, "Lizard")).isZero();
        Permanent token = findPermanent(player1, "Lizard");
        assertThat(token.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(8);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
