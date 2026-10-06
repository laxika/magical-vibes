package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestForTheWeary.class, Forest.class, GrizzlyBears.class})
class RestForTheWearyTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 4 life when no land entered under the caster's control")
    void gainsFourLifeWithoutLandfall() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Target player gains 8 life after the caster's landfall")
    void gainsEightLifeWithLandfall() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest(), new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.playLand(player1, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Landfall is checked when the spell resolves")
    void landfallIsCheckedAtResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster can target themselves without landfall")
    void canTargetCasterWithoutLandfall() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The caster can target themselves with landfall")
    void canTargetCasterWithLandfall() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest(), new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.playLand(player1, 0);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A land entering under the target opponent's control does not upgrade the spell")
    void opponentsLandDoesNotEnableLandfall() {
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("A nonland entering under the caster's control does not upgrade the spell")
    void nonlandDoesNotEnableLandfall() {
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Multiple lands entering still replace 4 life with exactly 8 life")
    void multipleLandsDoNotIncreaseLifeGainFurther() {
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RestForTheWeary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 28);
    }
}
