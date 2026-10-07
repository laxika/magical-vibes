package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.ChaosWarp;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinningStaff.class, CounselOfTheSoratami.class, SwarmIntelligence.class, ChaosWarp.class})
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

    @Test
    void opponentsStaffDoesNotAddCopies() {
        harness.addToBattlefield(player2, new TwinningStaff());
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(entry -> entry.getControllerId().equals(player1.getId()));
    }

    @Test
    void twoStaffsAddTwoCopiesInsteadOfRecursivelyCopying() {
        addReadyStaff();
        addReadyStaff();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 0, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3);
        assertThat(gd.stack).hasSize(4);
    }

    @Test
    void decliningSwarmIntelligenceCreatesNoAdditionalCopy() {
        addReadyStaff();
        harness.addToBattlefield(player1, new SwarmIntelligence());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    void abilityRequiresSevenManaAndTapsTheStaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new TwinningStaff());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, counsel.getId());

        assertThat(staff.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotTargetOpponentsInstant() {
        addReadyStaff();
        ChaosWarp warp = new ChaosWarp();
        harness.setHand(player2, List.of(warp));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, warp.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    void instantCopiesCanChooseTargetsIndependently() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new TwinningStaff());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SwarmIntelligence());
        ChaosWarp warp = new ChaosWarp();
        harness.setHand(player1, List.of(warp));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castInstant(player1, 0, other.getId());
        harness.activateAbility(player1, 0, 0, null, warp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, staff.getId());

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .extracting(StackEntry::getTargetId).containsExactlyInAnyOrder(other.getId(), staff.getId());
        assertThat(gd.stack).filteredOn(entry -> !entry.isCopy())
                .extracting(StackEntry::getTargetId).containsExactly(other.getId());
    }

    private void addReadyStaff() {
        addCreatureReady(player1, new TwinningStaff());
    }
}
