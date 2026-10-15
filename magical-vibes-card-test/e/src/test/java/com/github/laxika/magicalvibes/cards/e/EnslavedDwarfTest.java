package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.m.Mortiphobia;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnslavedDwarf.class, SengirVampire.class, AvenTrooper.class, Mortiphobia.class})
class EnslavedDwarfTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Enslaved Dwarf pumps a black creature and grants first strike")
    void sacrificesAndPumpsBlackCreature() {
        harness.addToBattlefield(player1, new EnslavedDwarf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Enslaved Dwarf");
        harness.assertInGraveyard(player1, "Enslaved Dwarf");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The pump and first strike wear off at cleanup")
    void effectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new EnslavedDwarf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        harness.addToBattlefield(player1, new EnslavedDwarf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Enslaved Dwarf");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target a black noncreature")
    void cannotTargetBlackNoncreature() {
        harness.addToBattlefield(player1, new EnslavedDwarf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mortiphobia());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Enslaved Dwarf");
        harness.assertOnBattlefield(player2, "Mortiphobia");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before the ability resolves, even when the Dwarf is tapped and summoning sick")
    void paysCostsBeforeResolutionWithoutTapRequirement() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new EnslavedDwarf());
        dwarf.tap();
        dwarf.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SengirVampire());
        int originalPower = gqs.getEffectivePower(gd, target);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Enslaved Dwarf");
        harness.assertInGraveyard(player1, "Enslaved Dwarf");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without red mana")
    void cannotPayWithOtherMana() {
        harness.addToBattlefield(player1, new EnslavedDwarf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Enslaved Dwarf");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }
}
