package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IhsansShade;
import com.github.laxika.magicalvibes.cards.r.RevekaWizardSavant;
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

@CardUsed({SerraPaladin.class, RevekaWizardSavant.class, IhsansShade.class})
class SerraPaladinTest extends BaseCardTest {

    private Permanent addPaladinReady() {
        return addCreatureReady(player1, new SerraPaladin());
    }

    @Test
    @DisplayName("Prevention ability prevents only the next 1 damage to a creature")
    void preventsNextDamageToCreature() {
        Permanent paladin = addPaladinReady();
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());

        harness.activateAbility(player1, 0, null, paladin.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, paladin.getId());
        harness.passBothPriorities();

        assertThat(paladin.getMarkedDamage()).isEqualTo(1);
        assertThat(reveka.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevention ability prevents only the next 1 damage to a player")
    void preventsNextDamageToPlayer() {
        Permanent paladin = addPaladinReady();
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(paladin.isTapped()).isTrue();
        assertThat(reveka.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevention effect expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        addPaladinReady();
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(reveka.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevention ability cannot target a creature with protection from white")
    void preventionCannotTargetProtectionFromWhite() {
        Permanent paladin = addPaladinReady();
        Permanent shade = addCreatureReady(player2, new IhsansShade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shade.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance ability grants vigilance to target creature")
    void grantsVigilance() {
        Permanent paladin = addPaladinReady();
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, reveka.getId());
        harness.passBothPriorities();

        assertThat(reveka.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOff() {
        Permanent paladin = addPaladinReady();
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, reveka.getId());
        harness.passBothPriorities();
        assertThat(reveka.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(reveka.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance ability cannot activate without enough mana")
    void vigilanceNeedsMana() {
        addPaladinReady();
        Permanent reveka = addCreatureReady(player1, new RevekaWizardSavant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, reveka.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vigilance ability can target only a creature")
    void vigilanceNeedsCreatureTarget() {
        addPaladinReady();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
