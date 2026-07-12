package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DramaticEntranceTest extends BaseCardTest {

    private void castEntrance() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Puts a chosen green creature onto the battlefield untapped")
    void putsGreenCreatureUntapped() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new GrizzlyBears()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, true);
        // Hand is now [Grizzly Bears]; put it onto the battlefield.
        harness.handleCardChosen(player1, 0);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new GrizzlyBears()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A non-green creature is not eligible to be put onto the battlefield")
    void nonGreenCreatureIsNotEligible() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new HillGiant()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }
}
