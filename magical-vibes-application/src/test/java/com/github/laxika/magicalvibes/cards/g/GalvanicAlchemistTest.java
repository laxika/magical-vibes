package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GalvanicAlchemistTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GalvanicAlchemist()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void tap(Permanent permanent) {
        permanent.tap();
    }

    private void activateUntap(Permanent permanent) {
        harness.addMana(player1, ManaColor.BLUE, 3);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulbond ETB pairs Galvanic Alchemist with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");

        assertThat(alchemist.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(alchemist.getId());
    }

    @Test
    @DisplayName("While paired, Galvanic Alchemist can untap itself")
    void pairedAlchemistCanUntapSelf() {
        castAndPairWithBears();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        tap(alchemist);
        assertThat(alchemist.isTapped()).isTrue();

        activateUntap(alchemist);

        assertThat(findPermanent(player1, "Galvanic Alchemist").isTapped()).isFalse();
    }

    @Test
    @DisplayName("While paired, the partner can untap itself")
    void pairedPartnerCanUntapSelf() {
        Permanent bears = castAndPairWithBears();
        tap(bears);
        assertThat(bears.isTapped()).isTrue();

        activateUntap(bears);

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unpaired Galvanic Alchemist does not have the untap ability")
    void unpairedCannotUntap() {
        harness.addToBattlefield(player1, new GalvanicAlchemist());
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        tap(alchemist);
        harness.addMana(player1, ManaColor.BLUE, 3);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(alchemist);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
