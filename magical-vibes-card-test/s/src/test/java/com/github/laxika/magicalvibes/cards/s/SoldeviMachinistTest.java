package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcticFoxes;
import com.github.laxika.magicalvibes.cards.b.BarbedSextant;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.z.ZuranEnchanter;
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

@CardUsed({SoldeviMachinist.class, IcyManipulator.class, ArcticFoxes.class, BarbedSextant.class, ZuranEnchanter.class})
class SoldeviMachinistTest extends BaseCardTest {

    private Permanent machinistOnBattlefield() {
        return addCreatureReady(player1, new SoldeviMachinist());
    }

    private void activateManaAbility() {
        harness.activateAbility(player1, 0, 0, null, null);
    }

    @Test
    @DisplayName("Tap ability adds two artifact-ability-only colorless")
    void tapAddsTwoArtifactAbilityOnlyColorless() {
        machinistOnBattlefield();

        activateManaAbility();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }

    @Test
    @CardUsed({IcyManipulator.class, ArcticFoxes.class})
    @DisplayName("Restricted mana pays an artifact's activated ability")
    void paysArtifactActivatedAbility() {
        machinistOnBattlefield();
        harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        Permanent foxes = harness.addToBattlefieldAndReturn(player2, new ArcticFoxes());

        activateManaAbility();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);

        // Icy Manipulator is at battlefield index 1; ability 0 costs {1}{T}
        harness.activateAbility(player1, 1, 0, null, foxes.getId());
        harness.passBothPriorities();

        assertThat(foxes.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(1);
    }

    @Test
    @CardUsed(BarbedSextant.class)
    @DisplayName("Restricted mana cannot pay an artifact spell")
    void cannotPayArtifactSpell() {
        machinistOnBattlefield();
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarbedSextant()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }

    @Test
    @CardUsed(ArcticFoxes.class)
    @DisplayName("Restricted mana cannot pay a non-artifact spell")
    void cannotPayNonArtifactSpell() {
        machinistOnBattlefield();
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new ArcticFoxes()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }

    @Test
    @CardUsed(ZuranEnchanter.class)
    @DisplayName("Restricted mana cannot pay a non-artifact's activated ability")
    void cannotPayNonArtifactActivatedAbility() {
        machinistOnBattlefield();
        Permanent enchanter = addCreatureReady(player1, new ZuranEnchanter());

        activateManaAbility();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(enchanter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana ability resolves immediately and taps its source")
    void resolvesImmediatelyAndTapsSource() {
        Permanent machinist = machinistOnBattlefield();

        activateManaAbility();

        assertThat(machinist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getArtifactAbilityOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Machinist cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent machinist = harness.addToBattlefieldAndReturn(player1, new SoldeviMachinist());
        machinist.setSummoningSick(true);

        assertThatThrownBy(this::activateManaAbility).isInstanceOf(IllegalStateException.class);

        assertThat(machinist.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("A tapped Machinist cannot produce mana again")
    void cannotActivateTwiceWithoutUntapping() {
        machinistOnBattlefield();
        activateManaAbility();

        assertThatThrownBy(this::activateManaAbility).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(2);
    }

    @Test
    @CardUsed(BarbedSextant.class)
    @DisplayName("Restricted mana pays an artifact mana ability even when its source is sacrificed")
    void paysArtifactManaAbilityWithSacrificeCost() {
        machinistOnBattlefield();
        harness.addToBattlefield(player1, new BarbedSextant());
        activateManaAbility();

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactAbilityOnlyColorless()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Barbed Sextant");
    }
}
