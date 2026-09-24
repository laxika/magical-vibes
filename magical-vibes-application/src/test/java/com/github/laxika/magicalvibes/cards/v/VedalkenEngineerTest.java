package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenEngineer.class, DarksteelIngot.class, DarksteelBrute.class,
        EchoingTruth.class, ViridianZealot.class})
class VedalkenEngineerTest extends BaseCardTest {

    private void addReadyEngineer() {
        addCreatureReady(player1, new VedalkenEngineer());
    }

    private void activateForBlue() {
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");
    }

    @Test
    @DisplayName("Tap ability adds two mana of the chosen color with the artifact restriction")
    void addsRestrictedChosenColor() {
        addReadyEngineer();

        activateForBlue();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact spell")
    void paysArtifactSpell() {
        addReadyEngineer();
        activateForBlue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarksteelBrute()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana pays for an artifact's activated ability")
    void paysArtifactActivatedAbility() {
        addReadyEngineer();
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateForBlue();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, brute)).isTrue();
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a non-artifact spell")
    void cannotPayNonArtifactSpell() {
        addReadyEngineer();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateForBlue();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a nonartifact activated ability")
    void cannotPayNonartifactActivatedAbility() {
        addReadyEngineer();
        addCreatureReady(player1, new ViridianZealot());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.addMana(player1, ManaColor.GREEN, 1);

        activateForBlue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Viridian Zealot");
    }
}
