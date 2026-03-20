package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardKickedOnlyManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElfhameDruidTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Elfhame Druid has two activated abilities")
    void hasCorrectAbilities() {
        ElfhameDruid card = new ElfhameDruid();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        ActivatedAbility first = card.getActivatedAbilities().get(0);
        assertThat(first.isRequiresTap()).isTrue();
        assertThat(first.getManaCost()).isNull();
        assertThat(first.getEffects()).hasSize(1);
        assertThat(first.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        ActivatedAbility second = card.getActivatedAbilities().get(1);
        assertThat(second.isRequiresTap()).isTrue();
        assertThat(second.getManaCost()).isNull();
        assertThat(second.getEffects()).hasSize(1);
        assertThat(second.getEffects().getFirst()).isInstanceOf(AwardKickedOnlyManaEffect.class);
    }

    // ===== First ability: {T}: Add {G} =====

    @Test
    @DisplayName("First ability adds one green mana")
    void firstAbilityAddsGreen() {
        harness.addToBattlefield(player1, new ElfhameDruid());

        Permanent druid = gd.playerBattlefields.get(player1.getId()).getFirst();
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(0);
    }

    // ===== Second ability: {T}: Add {G}{G}. Spend this mana only to cast kicked spells. =====

    @Test
    @DisplayName("Second ability adds two kicked-only green mana")
    void secondAbilityAddsKickedOnlyGreen() {
        harness.addToBattlefield(player1, new ElfhameDruid());

        Permanent druid = gd.playerBattlefields.get(player1.getId()).getFirst();
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(2);
    }

    // ===== Spending restriction: kicked-only green for kicked spells =====

    @Test
    @DisplayName("Kicked-only green can pay for a kicked creature spell")
    void kickedOnlyGreenPaysForKickedSpell() {
        harness.addToBattlefield(player1, new ElfhameDruid());

        Permanent druid = gd.playerBattlefields.get(player1.getId()).getFirst();
        druid.setSummoningSick(false);

        // Activate second ability: 2 kicked-only green
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Academy Drake: {2}{U} + kicker {4} = 1 blue + 6 generic
        // Pool: 2 kicked-only green + 1 blue + 4 colorless = 7 effective mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);

        // Should resolve without error — kicked-only green used for generic costs
        harness.passBothPriorities();

        // Academy Drake resolves and enters the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kicked-only green is not spent when casting a non-kicked spell")
    void kickedOnlyGreenNotUsedForNonKickedSpell() {
        harness.addToBattlefield(player1, new ElfhameDruid());

        Permanent druid = gd.playerBattlefields.get(player1.getId()).getFirst();
        druid.setSummoningSick(false);

        // Activate second ability: 2 kicked-only green
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Grizzly Bears ({1}{G}) with regular mana only
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Grizzly Bears enters the battlefield using regular green mana
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        // Kicked-only green should be untouched
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kicked-only green can pay the kicker cost portion")
    void kickedOnlyGreenPaysKickerCost() {
        harness.addToBattlefield(player1, new ElfhameDruid());

        Permanent druid = gd.playerBattlefields.get(player1.getId()).getFirst();
        druid.setSummoningSick(false);

        // Activate second ability: 2 kicked-only green
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Academy Drake: {2}{U} + kicker {4} = 7 mana total
        // Give enough regular mana for the main cost, let kicked-only green pay part of kicker
        // Main cost: {2}{U} = 3 mana. Kicker: {4} = 4 mana. Total: 7
        // Pool: 1 blue + 2 colorless (regular) + 2 kicked-only green = 5 regular + 2 KO green
        // Kicked-only green pays 2 of the generic costs
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        // Should resolve — kicked-only green helped cover total generic cost
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }
}
