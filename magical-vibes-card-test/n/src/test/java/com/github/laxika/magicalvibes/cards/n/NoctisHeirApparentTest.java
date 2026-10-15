package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoctisHeirApparent.class, GrizzlyBears.class, LeoninScimitar.class})
class NoctisHeirApparentTest extends BaseCardTest {

    @Test
    @DisplayName("May attach a controlled Equipment when a creature enters during combat")
    void attachesEquipmentWhenCreatureEntersDuringCombat() {
        addCreatureReady(player1, new NoctisHeirApparent());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Warp-Strike returns Noctis tapped, attacking, and unblockable")
    void warpStrikeReturnsTappedAttackingAndUnblockable() {
        Permanent noctis = addCreatureReady(player1, new NoctisHeirApparent());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(noctis);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(noctis.getCard());

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(noctis.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned).isNotSameAs(noctis);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasCantBeBlocked(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("A creature entering outside combat does not trigger the Equipment attachment")
    void doesNotTriggerOutsideCombat() {
        addCreatureReady(player1, new NoctisHeirApparent());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
