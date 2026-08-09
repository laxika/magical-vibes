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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadySarkhan(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
