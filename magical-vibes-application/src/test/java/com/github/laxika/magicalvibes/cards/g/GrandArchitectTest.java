package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.c.Cystbearer;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({GrandArchitect.class, CopperMyr.class, Cystbearer.class, SylvokLifestaff.class})
class GrandArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("Other blue creatures you control get +1/+1")
    void staticBoostOtherBlueCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrandArchitect());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrandArchitect());

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cystbearer());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Grand Architect does not boost itself")
    void doesNotBoostItself() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new GrandArchitect());
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

        Permanent myr = findPermanent(player1, "Copper Myr");
        // "Becomes blue" is a floating CR 613 layer-5 color setter.
        assertThat(gqs.getEffectiveColors(gd, myr)).containsExactly(CardColor.BLUE);
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

        Permanent myr = findPermanent(player1, "Copper Myr");
        // Copper Myr is 1/1, now blue → gets +1/+1 = 2/2
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Granted blue and its boost expire at cleanup")
    void grantedColorResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrandArchitect());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID myrId = harness.getPermanentId(player1, "Copper Myr");
        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Copper Myr");
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, myr)).doesNotContain(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple blue creatures presents choice for tap cost")
    void multipleBluePresentsTapChoice() {
        addCreatureReady(player1, new GrandArchitect());
        addCreatureReady(player1, new GrandArchitect());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Grand Architect can tap itself for {C}{C}")
    void canTapSelfForMana() {
        addCreatureReady(player1, new GrandArchitect());

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
        addCreatureReady(player1, new GrandArchitect());
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
        addCreatureReady(player1, new GrandArchitect());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Tap Grand Architect for {C}{C}
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);

        // Pay the colored requirement to isolate the restriction on generic mana.
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Cystbearer()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
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
        addCreatureReady(player1, new GrandArchitect());
        addCreatureReady(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Make Copper Myr blue
        UUID myrId = harness.getPermanentId(player1, "Copper Myr");
        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        Permanent myr = findPermanent(player1, "Copper Myr");
        assertThat(gqs.getEffectiveColors(gd, myr)).containsExactly(CardColor.BLUE);

        // Now 2 blue creatures — presents choice
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose Copper Myr to tap
        harness.handlePermanentChosen(player1, myrId);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No untapped blue creature to tap throws error")
    void noUntappedBlueCreatureThrows() {
        Permanent architect = addCreatureReady(player1, new GrandArchitect());
        architect.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Opposing artifact creatures can become blue without receiving the boost")
    void canMakeOpposingArtifactCreatureBlue() {
        harness.addToBattlefield(player1, new GrandArchitect());
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, myr.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, myr)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    @Test
    @DisplayName("The color ability rejects nonartifact creatures")
    void cannotTargetNonartifactCreature() {
        harness.addToBattlefield(player1, new GrandArchitect());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cystbearer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The color ability rejects noncreature artifacts")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new GrandArchitect());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restricted mana can pay an artifact's equip cost")
    void restrictedManaCanPayForArtifactAbilities() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new GrandArchitect());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 1, 0, null, architect.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(architect.getId());
        assertThat(gqs.getEffectivePower(gd, architect)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opposing blue creatures cannot pay the mana ability's tap cost")
    void cannotTapOpposingBlueCreatureForMana() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new GrandArchitect());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrandArchitect());
        architect.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(opponent.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Untapped nonblue creatures cannot pay the mana ability's tap cost")
    void cannotTapNonblueCreatureForMana() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new GrandArchitect());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        architect.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }
}
