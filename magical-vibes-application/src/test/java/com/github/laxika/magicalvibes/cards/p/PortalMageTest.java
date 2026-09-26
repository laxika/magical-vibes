package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortalMage.class, GrizzlyBears.class, NicolBolasPlaneswalker.class})
class PortalMageTest extends BaseCardTest {

    @Test
    @DisplayName("May reselect an attacking creature's target")
    void reselectsAttackTarget() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        int player2LifeBefore = gd.getLife(player2.getId());

        preparePortalMage();
        declareAttackers(List.of(0));

        castPortalMage(attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ReselectAttackTarget.class);
        harness.handlePermanentChosen(player1, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Declining leaves the attack target unchanged")
    void decliningLeavesAttackTargetUnchanged() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int player2LifeBefore = gd.getLife(player2.getId());
        preparePortalMage();
        declareAttackers(List.of(0));
        castPortalMage(attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
    }

    @Test
    @DisplayName("Cannot reselect the attacking creature's controller's permanent")
    void cannotChooseOwnPermanentAsAttackTarget() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        ownPermanent.setCounterCount(CounterType.LOYALTY, 5);
        preparePortalMage();
        declareAttackers(List.of(0));
        castPortalMage(attacker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger when it enters outside the declare attackers step")
    void doesNotTriggerOutsideDeclareAttackers() {
        harness.setHand(player1, List.of(new PortalMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castPortalMage(java.util.UUID attackerId) {
        harness.castCreature(player1, 0, attackerId);
    }

    private void preparePortalMage() {
        harness.setHand(player1, List.of(new PortalMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
