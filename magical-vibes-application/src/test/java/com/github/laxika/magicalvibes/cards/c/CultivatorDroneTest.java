package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalothPup;
import com.github.laxika.magicalvibes.cards.b.BearerOfSilence;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MyrGalvanizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CultivatorDrone.class, BalothPup.class, BearerOfSilence.class, MindStone.class, MyrGalvanizer.class})
class CultivatorDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Cultivator Drone produces restricted colorless mana")
    void tappingProducesRestrictedColorlessMana() {
        addCreatureReady(player1, new CultivatorDrone());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cultivator Drone mana can cast a colorless spell")
    void restrictedManaCanCastColorlessSpell() {
        addCreatureReady(player1, new CultivatorDrone());
        addCreatureReady(player1, new CultivatorDrone());

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
        addCreatureReady(player1, new CultivatorDrone());
        addCreatureReady(player1, new MyrGalvanizer());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isZero();
    }

    @Test
    @DisplayName("Cultivator Drone mana cannot cast a colored spell")
    void restrictedManaCannotCastColoredSpell() {
        addCreatureReady(player1, new CultivatorDrone());
        harness.activateAbility(player1, 0, null, null);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BalothPup()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Restricted mana pays the generic cost of a spell with devoid")
    void restrictedManaCanCastDevoidSpell() {
        addCreatureReady(player1, new CultivatorDrone());
        addCreatureReady(player1, new CultivatorDrone());
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CultivatorDrone()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted colorless mana cannot pay a colored symbol of a devoid spell")
    void restrictedManaCannotPayColoredSymbol() {
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new CultivatorDrone());
            harness.activateAbility(player1, i, null, null);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CultivatorDrone()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Restricted mana pays both the generic and colorless parts of a triggered cost containing {C}")
    void restrictedManaCanPayTriggeredCostContainingColorless() {
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new CultivatorDrone());
            harness.activateAbility(player1, i, null, null);
        }
        harness.addToBattlefield(player2, new BalothPup());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BearerOfSilence()));

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Baloth Pup");
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bearer of Silence");
    }

    @Test
    @DisplayName("A summoning-sick Cultivator Drone cannot activate its tap ability")
    void summoningSicknessPreventsManaAbility() {
        harness.addToBattlefield(player1, new CultivatorDrone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOrPermanentAbilityMana()).isZero();
    }
}
