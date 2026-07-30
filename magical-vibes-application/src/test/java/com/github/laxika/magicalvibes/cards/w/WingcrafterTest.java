package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WingcrafterTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Wingcrafter()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findWingcrafter() {
        return findPermanent(player1, "Wingcrafter");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Wingcrafter with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent wingcrafter = findWingcrafter();

        assertThat(wingcrafter.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(wingcrafter.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have flying")
    void pairedBothHaveFlying() {
        Permanent bears = castAndPairWithBears();
        Permanent wingcrafter = findWingcrafter();

        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Wingcrafter does not have flying")
    void unpairedHasNoFlying() {
        harness.addToBattlefield(player1, new Wingcrafter());
        Permanent wingcrafter = findWingcrafter();

        assertThat(wingcrafter.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without flying")
    void decliningLeavesUnpairedWithoutFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Wingcrafter()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wingcrafter = findWingcrafter();
        assertThat(wingcrafter.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, wingcrafter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
