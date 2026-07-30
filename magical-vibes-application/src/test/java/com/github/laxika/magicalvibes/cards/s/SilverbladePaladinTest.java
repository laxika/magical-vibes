package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilverbladePaladinTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SilverbladePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findPaladin() {
        return findPermanent(player1, "Silverblade Paladin");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Silverblade Paladin with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent paladin = findPaladin();

        assertThat(paladin.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(paladin.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have double strike")
    void pairedBothHaveDoubleStrike() {
        Permanent bears = castAndPairWithBears();
        Permanent paladin = findPaladin();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Silverblade Paladin does not have double strike")
    void unpairedHasNoDoubleStrike() {
        harness.addToBattlefield(player1, new SilverbladePaladin());
        Permanent paladin = findPaladin();

        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without double strike")
    void decliningLeavesUnpairedWithoutDoubleStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SilverbladePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent paladin = findPaladin();
        assertThat(paladin.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
