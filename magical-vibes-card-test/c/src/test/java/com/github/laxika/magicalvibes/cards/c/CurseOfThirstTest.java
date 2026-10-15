package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfThirst.class, CurseOfExhaustion.class, CurseOfEchoes.class})
class CurseOfThirstTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Curse of Thirst attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfThirst()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Thirst")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Deals 1 damage when it is the only Curse attached")
    void deals1DamageWhenOnlyCurse() {
        placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Deals damage equal to the number of Curses attached to the player")
    void dealsDamageEqualToCurseCount() {
        placeCurseOfThirstOnPlayer(player1, player2);
        // Two more Curses with no upkeep-damage triggers make three attached Curses.
        placeCurseOnPlayer(player1, player2, new CurseOfExhaustion());
        placeCurseOnPlayer(player1, player2, new CurseOfEchoes());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Resolve every triggered upkeep ability that landed on the stack.
        resolveAllTriggers();

        // Curse of Thirst deals 3, one for each Curse attached to the player.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Trigger does NOT fire during curse controller's upkeep")
    void triggerDoesNotFireDuringCurseControllerUpkeep() {
        placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("No damage trigger after curse is removed")
    void noTriggerAfterRemoval() {
        Permanent cursePerm = placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void countsCursesRegardlessOfControllerButOnlyOnEnchantedPlayer() {
        placeCurseOfThirstOnPlayer(player1, player2);
        placeCurseOnPlayer(player2, player2, new CurseOfExhaustion());
        placeCurseOnPlayer(player1, player1, new CurseOfEchoes());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void countsCursesAtResolutionAfterAnotherCurseLeaves() {
        placeCurseOfThirstOnPlayer(player1, player2);
        Permanent otherCurse = placeCurseOnPlayer(player1, player2, new CurseOfExhaustion());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(otherCurse);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void triggerStillDealsDamageAfterSourceLeaves() {
        Permanent thirst = placeCurseOfThirstOnPlayer(player1, player2);
        placeCurseOnPlayer(player1, player2, new CurseOfExhaustion());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(thirst);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void dealsNoDamageWhenNoCursesRemainAtResolution() {
        Permanent thirst = placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(thirst);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void canEnchantAndDamageItsController() {
        harness.setHand(player1, List.of(new CurseOfThirst()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    private Permanent placeCurseOfThirstOnPlayer(Player controller, Player enchantedPlayer) {
        return placeCurseOnPlayer(controller, enchantedPlayer, new CurseOfThirst());
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer, com.github.laxika.magicalvibes.model.Card curse) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, curse);
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}
