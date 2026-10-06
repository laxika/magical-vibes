package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AgonyWarp;
import com.github.laxika.magicalvibes.cards.g.Godsire;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RakeclawGargantuan.class, CylianElf.class, Godsire.class, ResoundingRoar.class, AgonyWarp.class})
class RakeclawGargantuanTest extends BaseCardTest {

    @Test
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RakeclawGargantuan());
        source.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent target = addCreatureReady(player2, new RakeclawGargantuan());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    void usesEffectivePowerForTargeting() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent target = addCreatureReady(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    void doesNotGrantFirstStrikeWhenTargetPowerFallsBelowFiveBeforeResolution() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent target = addCreatureReady(player1, new RakeclawGargantuan());
        Permanent elf = addCreatureReady(player1, new CylianElf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new AgonyWarp()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), elf.getId()));
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent source = addCreatureReady(player1, new RakeclawGargantuan());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Grants first strike to target creature with power 5 or greater")
    void grantsFirstStrikeToHighPowerCreature() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent target = addCreatureReady(player1, new Godsire());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, target.getId()).getGrantedKeywords())
                .contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent target = addCreatureReady(player1, new Godsire());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentById(gd, target.getId()).getGrantedKeywords())
                .contains(Keyword.FIRST_STRIKE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, target.getId()).getGrantedKeywords())
                .doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetLowPowerCreature() {
        addCreatureReady(player1, new RakeclawGargantuan());
        Permanent elf = addCreatureReady(player1, new CylianElf()); // 2/2
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
