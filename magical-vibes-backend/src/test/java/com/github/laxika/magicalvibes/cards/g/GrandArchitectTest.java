package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrandArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("Other blue creatures you control get +1/+1")
    void staticBoostOtherBlueCreatures() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new GrandArchitect());

        Permanent first = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);

        // Each Grand Architect (1/3) boosts the other → 2/4
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-blue creatures do not get +1/+1")
    void staticBoostDoesNotAffectNonBlue() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent elves = gd.playerBattlefields.get(player1.getId()).get(1);
        // Llanowar Elves is 1/1 green — no boost from Grand Architect
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    @Test
    @DisplayName("Grand Architect does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new GrandArchitect());

        Permanent architect = gd.playerBattlefields.get(player1.getId()).get(0);
        // Grand Architect is 1/3 — no self-boost
        assertThat(gqs.getEffectivePower(gd, architect)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, architect)).isEqualTo(3);
    }

    @Test
    @DisplayName("{U}: Target artifact creature becomes blue until end of turn")
    void grantColorToArtifactCreature() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID myrId = harness.getPermanentId(player1, "Copper Myr");
        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();
        assertThat(myr.getGrantedColors()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Artifact creature that becomes blue gets +1/+1 from static")
    void grantedBlueGetsStaticBoost() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID myrId = harness.getPermanentId(player1, "Copper Myr");
        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();
        // Copper Myr is 1/1, now blue → gets +1/+1 = 2/2
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Granted blue wears off after resetModifiers")
    void grantedColorResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID myrId = harness.getPermanentId(player1, "Copper Myr");
        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();
        myr.resetModifiers();

        assertThat(myr.getGrantedColors()).isEmpty();
    }

    @Test
    @DisplayName("Multiple blue creatures presents choice for tap cost")
    void multipleBluePresentsTapChoice() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new GrandArchitect());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Grand Architect can tap itself for {C}{C}")
    void canTapSelfForMana() {
        harness.addToBattlefield(player1, new GrandArchitect());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        // Only one blue creature — auto-taps itself
        harness.activateAbility(player1, 0, 1, null, null);

        // Mana ability resolves immediately
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);

        Permanent architect = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(architect.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restricted mana can pay for artifact spells")
    void restrictedManaCanPayForArtifacts() {
        harness.addToBattlefield(player1, new GrandArchitect());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Tap Grand Architect for {C}{C}
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);

        // Copper Myr costs {2} — castable with artifact-only mana
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(0);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for non-artifact spells")
    void restrictedManaCannotPayForNonArtifacts() {
        harness.addToBattlefield(player1, new GrandArchitect());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Tap Grand Architect for {C}{C}
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);

        // Llanowar Elves costs {G} — restricted mana can't help
        harness.setHand(player1, List.of(new LlanowarElves()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Mana ability works with summoning sick creature (no tap symbol cost)")
    void manaAbilityWorksWithSummoningSick() {
        harness.addToBattlefield(player1, new GrandArchitect());
        // Don't remove summoning sickness — the ability doesn't have {T} in cost

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combo: make artifact creature blue then tap it for mana")
    void comboMakeBlueAndTapForMana() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new CopperMyr());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Make Copper Myr blue
        UUID myrId = harness.getPermanentId(player1, "Copper Myr");
        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Copper Myr"))
                .findFirst().orElseThrow();
        assertThat(myr.getGrantedColors()).contains(CardColor.BLUE);

        // Now 2 blue creatures — presents choice
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose Copper Myr to tap
        harness.handlePermanentChosen(player1, myrId);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No untapped blue creature to tap throws error")
    void noUntappedBlueCreatureThrows() {
        harness.addToBattlefield(player1, new GrandArchitect());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> {
            p.setSummoningSick(false);
            p.tap();
        });

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }
}
