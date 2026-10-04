package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopyTactician.class, ElvishWarrior.class, GrizzlyBears.class})
class CanopyTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void buffsOtherElvesYouControl() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent elf = findPermanent(player1, "Elvish Warrior");
        int basePower = gqs.getEffectivePower(gd, elf);
        int baseToughness = gqs.getEffectiveToughness(gd, elf);

        harness.addToBattlefield(player1, new CanopyTactician());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Canopy Tactician does not buff itself")
    void doesNotBuffItself() {
        CanopyTactician card = new CanopyTactician();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent tactician = findPermanent(player1, "Canopy Tactician");

        assertThat(gqs.getEffectivePower(gd, tactician)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, tactician)).isEqualTo(10);
    }

    @Test
    @DisplayName("Does not buff non-Elf creatures")
    void doesNotBuffNonElves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new CanopyTactician());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff an opponent's Elves")
    void doesNotBuffOpponentsElves() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        Permanent elf = findPermanent(player2, "Elvish Warrior");
        int basePower = gqs.getEffectivePower(gd, elf);
        int baseToughness = gqs.getEffectiveToughness(gd, elf);

        harness.addToBattlefield(player1, new CanopyTactician());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Tapping Canopy Tactician adds three green mana")
    void tappingAddsThreeGreenMana() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new CanopyTactician());
        tactician.setSummoningSick(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }
}
