package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaazdaShieldMate.class, GoblinPiker.class})
class HaazdaShieldMateTest extends BaseCardTest {

    @Test
    @DisplayName("Declining its upkeep payment sacrifices Haazda Shield Mate")
    void decliningUpkeepPaymentSacrificesIt() {
        Permanent shieldMate = harness.addToBattlefieldAndReturn(player1, new HaazdaShieldMate());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shieldMate);
        harness.assertInGraveyard(player1, "Haazda Shield Mate");
    }

    @Test
    @DisplayName("Paying its upkeep cost keeps Haazda Shield Mate on the battlefield")
    void payingUpkeepPaymentKeepsIt() {
        Permanent shieldMate = harness.addToBattlefieldAndReturn(player1, new HaazdaShieldMate());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldMate);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsNextDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyShieldMate(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void damageFromDifferentSourceIsNotPrevented() {
        harness.setLife(player1, 20);
        addReadyShieldMate(player1);
        Permanent chosenSource = addReadyGoblin(player2);
        Permanent otherSource = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(chosenSource.getId()));
    }

    private Permanent addReadyShieldMate(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new HaazdaShieldMate());
    }

    private Permanent addReadyGoblin(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GoblinPiker());
    }
}
