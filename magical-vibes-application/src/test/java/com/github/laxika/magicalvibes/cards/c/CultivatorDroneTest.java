package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MyrGalvanizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CultivatorDrone.class, GrizzlyBears.class, MindStone.class, MyrGalvanizer.class})
class CultivatorDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Cultivator Drone produces restricted colorless mana")
    void tappingProducesRestrictedColorlessMana() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new CultivatorDrone());
        drone.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cultivator Drone mana can cast a colorless spell")
    void restrictedManaCanCastColorlessSpell() {
        Permanent firstDrone = harness.addToBattlefieldAndReturn(player1, new CultivatorDrone());
        Permanent secondDrone = harness.addToBattlefieldAndReturn(player1, new CultivatorDrone());
        firstDrone.setSummoningSick(false);
        secondDrone.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MindStone()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isZero();
    }

    @Test
    @DisplayName("Cultivator Drone mana can activate an ability of a colorless permanent")
    void restrictedManaCanActivateColorlessPermanentAbility() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new CultivatorDrone());
        Permanent galvanizer = harness.addToBattlefieldAndReturn(player1, new MyrGalvanizer());
        drone.setSummoningSick(false);
        galvanizer.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isZero();
    }

    @Test
    @DisplayName("Cultivator Drone mana cannot cast a colored spell")
    void restrictedManaCannotCastColoredSpell() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new CultivatorDrone());
        drone.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
