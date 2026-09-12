package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Caltrops.class, ElvishLookout.class, PlatedSpider.class})
class CaltropsTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers once per attacking creature, on the Caltrops controller's side")
    void triggersOnAttack() {
        harness.addToBattlefield(player1, new Caltrops());
        Permanent attacker = addCreatureReady(player2, new PlatedSpider());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Deals 1 damage to an opponent's attacker")
    void damagesOpponentAttacker() {
        harness.addToBattlefield(player1, new Caltrops());
        Permanent attacker = addCreatureReady(player2, new PlatedSpider());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to the controller's own attacker too")
    void damagesOwnAttacker() {
        harness.addToBattlefield(player1, new Caltrops());
        Permanent attacker = addCreatureReady(player1, new PlatedSpider());

        // Caltrops is at index 0, the attacking Plated Spider at index 1.
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fires once per attacker, damaging each")
    void firesOncePerAttacker() {
        harness.addToBattlefield(player1, new Caltrops());
        Permanent attacker1 = addCreatureReady(player2, new PlatedSpider());
        Permanent attacker2 = addCreatureReady(player2, new PlatedSpider());

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(attacker1.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Caltrops triggers independently for the same attacker")
    void eachCopyDamagesTheAttacker() {
        harness.addToBattlefield(player1, new Caltrops());
        harness.addToBattlefield(player1, new Caltrops());
        Permanent attacker = addCreatureReady(player2, new PlatedSpider());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damages a shrouded attacker because the ability does not target")
    void damagesShroudedAttacker() {
        harness.addToBattlefield(player1, new Caltrops());
        Permanent attacker = addCreatureReady(player2, new ElvishLookout());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Elvish Lookout");
    }
}
