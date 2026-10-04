package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.p.PenregonStrongbull;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBlastRunner.class, Atog.class, Spellbook.class,
        PenregonStrongbull.class, EnergyRefractor.class})
class GoblinBlastRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and menace after you sacrifice a permanent")
    void getsBonusAfterSacrifice() {
        harness.addToBattlefield(player1, new Atog());
        Permanent blastRunner = harness.addToBattlefieldAndReturn(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blastRunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The bonus ends with the turn")
    void bonusEndsWithTurn() {
        harness.addToBattlefield(player1, new Atog());
        Permanent blastRunner = harness.addToBattlefieldAndReturn(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's sacrifice does not grant the bonus")
    void opponentSacrificeDoesNotGrantBonus() {
        Permanent blastRunner = harness.addToBattlefieldAndReturn(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player2, new Atog());
        harness.addToBattlefield(player2, new Spellbook());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The bonus applies as soon as the sacrifice cost is paid")
    void bonusAppliesBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        Permanent blastRunner = harness.addToBattlefieldAndReturn(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isFalse();
        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Energy Refractor");
        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blastRunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A sacrifice before Blast-Runner enters still grants the bonus")
    void earlierSacrificeGrantsBonusOnEntry() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent blastRunner = harness.enterBattlefieldAndReturn(player1, new GoblinBlastRunner());

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blastRunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Multiple sacrifices grant the bonus only once")
    void multipleSacrificesDoNotStackBonus() {
        harness.addToBattlefield(player1, new PenregonStrongbull());
        Permanent blastRunner = harness.addToBattlefieldAndReturn(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blastRunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isTrue();
    }
}
