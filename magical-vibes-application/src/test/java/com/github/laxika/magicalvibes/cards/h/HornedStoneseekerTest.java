package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HornedStoneseekerTest extends BaseCardTest {

    @Test
    @DisplayName("When Horned Stoneseeker enters, it creates a tapped Powerstone token")
    void entersCreatesTappedPowerstone() {
        harness.setHand(player1, List.of(new HornedStoneseeker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
        assertThat(powerstones.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstones.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
    }

    @Test
    @DisplayName("When Horned Stoneseeker leaves, its controller sacrifices a Powerstone")
    void leavesSacrificesPowerstone() {
        harness.setHand(player1, List.of(new HornedStoneseeker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Horned Stoneseeker")).isZero();
        assertThat(countPermanents(player1, "Powerstone")).isZero();
    }
}
