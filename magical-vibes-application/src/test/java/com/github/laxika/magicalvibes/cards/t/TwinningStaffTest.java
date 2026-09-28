package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.s.SwarmIntelligence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinningStaff.class, CounselOfTheSoratami.class, SwarmIntelligence.class})
class TwinningStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one copy to an activated spell copy")
    void addsCopyToActivatedSpellCopy() {
        addReadyStaff();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 0, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL).hasSize(3);
    }

    @Test
    @DisplayName("Adds one copy to a triggered spell copy")
    void addsCopyToTriggeredSpellCopy() {
        addReadyStaff();
        harness.addToBattlefield(player1, new SwarmIntelligence());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL).hasSize(3);
    }

    private void addReadyStaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new TwinningStaff());
        staff.setSummoningSick(false);
    }
}
