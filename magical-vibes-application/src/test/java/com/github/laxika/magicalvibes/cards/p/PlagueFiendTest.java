package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RibCageSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueFiend.class, RibCageSpider.class})
class PlagueFiendTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged creature's controller may pay {2} to survive")
    void payingPreventsDestruction() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertOnBattlefield(player2, "Rib Cage Spider");
    }

    @Test
    @DisplayName("The damaged creature is destroyed when its controller declines to pay")
    void decliningDestroysDamagedCreature() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Rib Cage Spider");
        harness.assertInGraveyard(player2, "Rib Cage Spider");
    }

    @Test
    @DisplayName("The damaged creature is destroyed when its controller cannot pay")
    void cannotPayDestroysDamagedCreature() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Rib Cage Spider");
        harness.assertInGraveyard(player2, "Rib Cage Spider");
    }

    @Test
    @DisplayName("Triggers when Plague Fiend deals combat damage while blocking")
    void triggersWhenBlockingCreature() {
        addCreatureReady(player1, new PlagueFiend());
        Permanent attacker = addCreatureReady(player2, new RibCageSpider());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Rib Cage Spider");
        harness.assertInGraveyard(player2, "Rib Cage Spider");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Plague Fiend")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombat();

        harness.assertOnBattlefield(player2, "Rib Cage Spider");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void resolveCombatToPaymentChoice() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
    }
}
