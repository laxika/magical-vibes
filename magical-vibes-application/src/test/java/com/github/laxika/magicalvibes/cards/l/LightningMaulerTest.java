package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightningMaulerTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningMauler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findMauler() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lightning Mauler"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Soulbond ETB pairs Lightning Mauler with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent mauler = findMauler();

        assertThat(mauler.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(mauler.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have haste")
    void pairedBothHaveHaste() {
        Permanent bears = castAndPairWithBears();
        Permanent mauler = findMauler();

        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Lightning Mauler does not have haste")
    void unpairedHasNoHaste() {
        harness.addToBattlefield(player1, new LightningMauler());
        Permanent mauler = findMauler();

        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without haste")
    void decliningLeavesUnpairedWithoutHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningMauler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent mauler = findMauler();
        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
