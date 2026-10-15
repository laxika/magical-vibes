package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrievousWound.class, AngelOfMercy.class, Shock.class, GrizzlyBears.class})
class GrievousWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Grievous Wound attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new GrievousWound()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof GrievousWound
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Only the enchanted player is prevented from gaining life")
    void onlyEnchantedPlayerCannotGainLife() {
        placeGrievousWound(player1, player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castAngelOfMercy(player2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        castAngelOfMercy(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Damage to the enchanted player makes them lose half their life, rounded up")
    void damageTriggersHalfLifeLoss() {
        placeGrievousWound(player1, player2);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Multiple creatures dealing combat damage cause only one trigger")
    void combatDamageTriggersOnceForMultipleAttackers() {
        placeGrievousWound(player1, player2);
        harness.setLife(player2, 20);
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Half of an odd life total is rounded up after damage")
    void oddLifeTotalRoundsLifeLossUp() {
        placeGrievousWound(player1, player2);
        harness.setLife(player2, 11);

        castShock(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to the other player does not trigger Grievous Wound")
    void damageToOtherPlayerDoesNotTrigger() {
        placeGrievousWound(player1, player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castShock(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Wound uses the life total when its trigger resolves")
    void multipleWoundsUseCurrentLifeTotal() {
        placeGrievousWound(player1, player2);
        placeGrievousWound(player1, player2);
        harness.setLife(player2, 20);

        castShock(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A trigger still resolves after its Aura leaves the battlefield")
    void triggerSurvivesAuraLeavingBattlefield() {
        Permanent aura = placeGrievousWound(player1, player2);
        harness.setLife(player2, 20);

        castShock(player2);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        castAngelOfMercy(player2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The Aura can enchant its controller and triggers for their damage")
    void canEnchantController() {
        harness.setHand(player1, List.of(new GrievousWound()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);

        castShock(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
        castAngelOfMercy(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
    }

    private void castShock(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent placeGrievousWound(Player controller, Player enchantedPlayer) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new GrievousWound());
        aura.setAttachedTo(enchantedPlayer.getId());
        return aura;
    }

    private void castAngelOfMercy(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
