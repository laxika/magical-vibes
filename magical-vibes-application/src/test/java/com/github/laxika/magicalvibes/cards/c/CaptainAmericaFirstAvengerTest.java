package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlowstoneCrusher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainAmericaFirstAvenger.class, GrizzlyBears.class, StriderHarness.class, FlowstoneCrusher.class, LeoninScimitar.class, LoxodonWarhammer.class})
class CaptainAmericaFirstAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Throw unattaches an Equipment and deals its mana value divided among targets")
    void throwDealsEquipmentManaValueDividedAmongTargets() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaFirstAvenger());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        hammer.setAttachedTo(captain.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(target.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isNull();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Throw lets the controller choose which attached Equipment to unattach")
    void throwChoosesAttachedEquipment() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaFirstAvenger());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(captain.getId());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        hammer.setAttachedTo(captain.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(scimitar.getId(), hammer.getId());
        harness.handlePermanentChosen(player1, hammer.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isNull();
        assertThat(scimitar.getAttachedTo()).isEqualTo(captain.getId());
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Catch attaches up to one target Equipment you control at beginning of combat")
    void catchAttachesTargetEquipmentAtBeginningOfCombat() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaFirstAvenger());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(scimitar.getId());
        harness.handlePermanentChosen(player1, scimitar.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(captain.getId());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
