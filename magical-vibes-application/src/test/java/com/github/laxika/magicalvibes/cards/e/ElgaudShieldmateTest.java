package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElgaudShieldmateTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElgaudShieldmate()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findShieldmate() {
        return findPermanent(player1, "Elgaud Shieldmate");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Elgaud Shieldmate with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent shieldmate = findShieldmate();

        assertThat(shieldmate.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(shieldmate.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have hexproof")
    void pairedBothHaveHexproof() {
        Permanent bears = castAndPairWithBears();
        Permanent shieldmate = findShieldmate();

        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Elgaud Shieldmate does not have hexproof")
    void unpairedHasNoHexproof() {
        harness.addToBattlefield(player1, new ElgaudShieldmate());
        Permanent shieldmate = findShieldmate();

        assertThat(shieldmate.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without hexproof")
    void decliningLeavesUnpairedWithoutHexproof() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElgaudShieldmate()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent shieldmate = findShieldmate();
        assertThat(shieldmate.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, shieldmate, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
