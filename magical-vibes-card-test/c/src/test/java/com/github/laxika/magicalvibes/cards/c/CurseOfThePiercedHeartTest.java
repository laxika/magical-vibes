package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GarrukRelentless;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfThePiercedHeart.class, GarrukRelentless.class})
class CurseOfThePiercedHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Curse of the Pierced Heart targeting a player")
    void canCastTargetingPlayer() {
        harness.setHand(player1, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Curse of the Pierced Heart attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Pierced Heart")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Enchanted player takes 1 damage at their upkeep")
    void enchantedPlayerTakes1DamageAtUpkeep() {
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Trigger does NOT fire during curse controller's upkeep")
    void triggerDoesNotFireDuringCurseControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Two curses deal 2 total damage at enchanted player's upkeep")
    void twoCursesDeal2Damage() {
        placeCurseOnPlayer(player1, player2);
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("No damage trigger after curse is removed")
    void noTriggerAfterRemoval() {
        Permanent cursePerm = placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new CurseOfThePiercedHeart());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }

    @Test
    @DisplayName("The controller can choose a planeswalker controlled by the enchanted player")
    void canDamageEnchantedPlayersPlaneswalker() {
        placeCurseOnPlayer(player1, player2);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukRelentless());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(planeswalker.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A curse enchanting its controller damages that player at their upkeep")
    void canEnchantItsController() {
        placeCurseOnPlayer(player1, player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Removing the curse after its upkeep trigger does not stop the damage")
    void triggerResolvesAfterCurseLeavesBattlefield() {
        Permanent curse = placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }
}
