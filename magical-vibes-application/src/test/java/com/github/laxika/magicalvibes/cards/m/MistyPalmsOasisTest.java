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

    private Permanent addOasisReady(Player player) {
        Permanent permanent = new Permanent(new MistyPalmsOasis());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
