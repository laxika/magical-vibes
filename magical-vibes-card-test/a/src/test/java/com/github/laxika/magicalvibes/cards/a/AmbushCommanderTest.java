package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmbushCommander.class, ElvishMystic.class, Forest.class, GrizzlyBears.class, Mountain.class})
class AmbushCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Only your Forests become 1/1 green Elf creatures that are still lands")
    void animatesForests() {
        Permanent forest1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent forest2 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new AmbushCommander());

        assertThat(gqs.isCreature(gd, forest1)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest1)).isEqualTo(1);
        assertThat(gqs.isLand(gd, forest1)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, forest1)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest1)).contains(CardSubtype.ELF);

        assertThat(gqs.isCreature(gd, forest2)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest2)).doesNotContain(CardSubtype.ELF);
        assertThat(gqs.isLand(gd, forest2)).isTrue();

        assertThat(gqs.isCreature(gd, mountain)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing an Elf gives the target creature +3/+3 until end of turn")
    void sacrificesElfAndBoostsTargetCreature() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, elf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Elvish Mystic");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, elf.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new AmbushCommander());
        harness.addToBattlefield(player1, new ElvishMystic());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Sacrificing an Elf gives a target creature +3/+3 until end of turn")
    void sacrificesElfToBoostTarget() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The commander itself can be sacrificed as the Elf cost")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ambush Commander");
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An animated Forest can be targeted as a creature")
    void canTargetAnimatedForest() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handlePermanentChosen(player1, elf.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.isLand(gd, forest)).isTrue();
        harness.assertInGraveyard(player1, "Elvish Mystic");
    }

    @Test
    @DisplayName("Sacrificing the commander ends its animation before the ability resolves")
    void sacrificingCommanderMakesForestTargetIllegal() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new AmbushCommander());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handlePermanentChosen(player1, commander.getId());

        harness.assertInGraveyard(player1, "Ambush Commander");
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).doesNotContain(CardSubtype.ELF);

        harness.passBothPriorities();
        harness.addToBattlefield(player1, new AmbushCommander());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
    }

    @Test
    @DisplayName("A second commander keeps the Forest target animated when the first is sacrificed")
    void secondCommanderKeepsForestTargetLegal() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new AmbushCommander());
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handlePermanentChosen(player1, commander.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
    }

    @Test
    @DisplayName("The target Elf may itself be sacrificed and the ability then has no legal target")
    void canSacrificeTargetElf() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Ambush Commander");
        assertThat(gd.stack).isEmpty();
    }
}
