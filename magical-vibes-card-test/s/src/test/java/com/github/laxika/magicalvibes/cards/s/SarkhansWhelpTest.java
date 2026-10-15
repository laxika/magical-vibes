package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Rootwalla;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhansWhelp.class, SarkhanFireblood.class, Rootwalla.class, GrizzlyBears.class})
class SarkhansWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target when you activate a Sarkhan planeswalker's ability")
    void dealsDamageWhenSarkhanAbilityIsActivated() {
        addCreatureReady(player1, new SarkhansWhelp());
        addReadySarkhan(player1, new SarkhanFireblood());

        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 1, 0, null, null);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger for an ability of a non-Sarkhan permanent")
    void doesNotTriggerForNonSarkhanAbility() {
        addCreatureReady(player1, new SarkhansWhelp());
        addCreatureReady(player1, new Rootwalla());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Can deal the triggered damage to a creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new SarkhansWhelp());
        addReadySarkhan(player1, new SarkhanFireblood());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent activates a Sarkhan ability")
    void doesNotTriggerForOpponentSarkhanActivation() {
        addCreatureReady(player1, new SarkhansWhelp());
        addReadySarkhan(player2, new SarkhanFireblood());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Whelp can damage the Sarkhan whose ability triggered it")
    void canTargetActivatingPlaneswalker() {
        addCreatureReady(player1, new SarkhansWhelp());
        Permanent sarkhan = addReadySarkhan(player1, new SarkhanFireblood());

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sarkhan.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Whelp triggers independently for one Sarkhan activation")
    void multipleWhelpsEachDealDamage() {
        addCreatureReady(player1, new SarkhansWhelp());
        addCreatureReady(player1, new SarkhansWhelp());
        addReadySarkhan(player1, new SarkhanFireblood());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The Whelp still triggers when Sarkhan spends all his loyalty")
    void triggersWhenActivatingPlaneswalkerDies() {
        addCreatureReady(player1, new SarkhansWhelp());
        Permanent sarkhan = addReadySarkhan(player1, new SarkhanFireblood());
        sarkhan.setCounterCount(CounterType.LOYALTY, 7);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sarkhan);
    }

    private Permanent addReadySarkhan(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
