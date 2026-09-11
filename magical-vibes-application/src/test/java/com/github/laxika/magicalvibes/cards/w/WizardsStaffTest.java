package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChargingPaladin;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardsStaff.class, ChargingPaladin.class, FugitiveWizard.class, GrizzlyBears.class})
class WizardsStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has prowess")
    void equippedCreatureHasProwess() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new WizardsStaff());
        staff.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.PROWESS)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature's triggered ability triggers an additional time")
    void doublesEquippedCreatureTriggers() {
        Permanent paladin = addCreatureReady(player1, new ChargingPaladin());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new WizardsStaff());
        staff.setAttachedTo(paladin.getId());

        declareAttackers(player1, java.util.List.of(0));
        resolveAllTriggers();

        assertThat(paladin.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("Equip Wizard {1} only targets Wizards and Equip {3} targets any creature")
    void supportsBothEquipAbilities() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new WizardsStaff());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 0, null, wizard.getId());
        harness.passBothPriorities();
        assertThat(staff.getAttachedTo()).isEqualTo(wizard.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        assertThat(staff.getAttachedTo()).isEqualTo(bear.getId());
    }
}
