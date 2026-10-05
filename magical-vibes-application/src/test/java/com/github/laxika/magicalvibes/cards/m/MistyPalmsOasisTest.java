package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistyPalmsOasis.class, GrizzlyBears.class})
class MistyPalmsOasisTest extends BaseCardTest {

    @Test
    @DisplayName("Misty Palms Oasis enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MistyPalmsOasis()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent oasis = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(oasis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Misty Palms Oasis for white mana produces one white")
    void tappingProducesWhiteMana() {
        Permanent oasis = addOasisReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(oasis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Misty Palms Oasis for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent oasis = addOasisReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(oasis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana sacrifices Misty Palms Oasis and draws a card")
    void payingFourManaSacrificesAndDraws() {
        Permanent oasis = addOasisReady(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oasis);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oasis.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(1);
    }

    @Test
    void tappedOasisCannotActivateAnyAbility() {
        Permanent oasis = addOasisReady(player1);
        oasis.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int abilityIndex = 0; abilityIndex < 3; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oasis);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void insufficientManaDoesNotSacrificeOrTapOasis() {
        Permanent oasis = addOasisReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(oasis.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oasis);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void drawWaitsForResolutionAndOnlyControllerDraws() {
        Permanent oasis = addOasisReady(player2);
        MistyPalmsOasis drawnCard = new MistyPalmsOasis();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.WHITE, 4);
        int opponentHandSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, 2, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oasis);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(oasis.getCard());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSize);
    }
    private Permanent addOasisReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MistyPalmsOasis());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
