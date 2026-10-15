package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeditationPools.class})
class MeditationPoolsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MeditationPools()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Meditation Pools").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping it adds green or blue mana")
    void tappingAddsChosenMana() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new MeditationPools());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(pools.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana sacrifices it and draws a card")
    void payingFourManaSacrificesAndDraws() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new MeditationPools());
        harness.setLibrary(player1, List.of(new MeditationPools()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pools);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pools.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Meditation Pools");
    }

    @Test
    @DisplayName("Green mana is available immediately without using the stack")
    void tappingAddsGreenManaImmediately() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new MeditationPools());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(pools.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land that just entered tapped cannot activate either ability")
    void tappedLandCannotActivateEitherAbility() {
        harness.setHand(player1, List.of(new MeditationPools()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Permanent pools = findPermanent(player1, "Meditation Pools");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pools);
        harness.assertNotInGraveyard(player1, "Meditation Pools");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana cannot pay the draw ability and does not sacrifice the land")
    void insufficientManaLeavesLandUntappedAndUnsacrificed() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new MeditationPools());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pools.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pools);
        harness.assertNotInGraveyard(player1, "Meditation Pools");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana can pay the generic draw cost and only the controller draws")
    void coloredManaPaysDrawCostForController() {
        Permanent pools = harness.addToBattlefieldAndReturn(player2, new MeditationPools());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new MeditationPools()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(pools.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Meditation Pools");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Meditation Pools");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
