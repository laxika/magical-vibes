package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElectrodominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage and offers a nonland hand spell with mana value X or less")
    void dealsDamageAndOffersEligibleHandSpell() {
        Electrodominance electrodominance = new Electrodominance();
        GrizzlyBears eligibleSpell = new GrizzlyBears();
        HillGiant tooExpensiveSpell = new HillGiant();
        Forest land = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(electrodominance, eligibleSpell, tooExpensiveSpell, land)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligibleSpell.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Does not offer hand spells whose mana value exceeds X")
    void noOfferForSpellsAboveX() {
        Electrodominance electrodominance = new Electrodominance();
        HillGiant tooExpensiveSpell = new HillGiant();
        harness.setHand(player1, new ArrayList<>(List.of(electrodominance, tooExpensiveSpell)));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(tooExpensiveSpell);
    }

    @Test
    @DisplayName("Declining the free cast leaves the eligible spell in hand")
    void decliningFreeCastLeavesSpellInHand() {
        Electrodominance electrodominance = new Electrodominance();
        GrizzlyBears eligibleSpell = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(electrodominance, eligibleSpell)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleSpell);
    }
}
