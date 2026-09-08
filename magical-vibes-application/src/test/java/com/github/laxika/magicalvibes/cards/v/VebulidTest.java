package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VebulidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent vebulid = castVebulid(player1);

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May put a +1/+1 counter on itself at upkeep")
    void mayPutCounterAtUpkeep() {
        Permanent vebulid = addReadyVebulid(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("May decline the upkeep counter")
    void mayDeclineCounterAtUpkeep() {
        Permanent vebulid = addReadyVebulid(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking destroys Vebulid at end of combat")
    void attackingDestroysItAtEndOfCombat() {
        Permanent vebulid = castVebulid(player1);
        vebulid.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vebulid");
        harness.assertInGraveyard(player1, "Vebulid");
    }

    @Test
    @DisplayName("Blocking schedules Vebulid for end-of-combat destruction")
    void blockingSchedulesDestruction() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent vebulid = addReadyVebulid(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Vebulid"));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(vebulid.getId()));
    }

    private Permanent castVebulid(Player player) {
        harness.setHand(player, List.of(new Vebulid()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "Vebulid");
    }

    private Permanent addReadyVebulid(Player player) {
        Permanent vebulid = addReady(player, new Vebulid());
        vebulid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return vebulid;
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
