package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NicolBolas.class, FlyingMen.class, FireWhip.class})
class NicolBolasTest extends BaseCardTest {

    @Test
    @DisplayName("The upkeep payment does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotRequirePayment() {
        harness.addToBattlefield(player1, new NicolBolas());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Nicol Bolas");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three mana of the wrong colors cannot pay the upkeep cost")
    void wrongColorsCannotPayUpkeep() {
        harness.addToBattlefield(player1, new NicolBolas());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Nicol Bolas");
        harness.assertInGraveyard(player1, "Nicol Bolas");
    }

    @Test
    @DisplayName("Damage to an opponent's creature does not make that opponent discard")
    void damageToCreatureDoesNotDiscard() {
        harness.setHand(player2, List.of(new FlyingMen(), new FlyingMen()));
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new FlyingMen());
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(bolas.getId());

        harness.activateAbility(player1, 0, null, victim.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Flying Men");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Damage to an opponent with an empty hand resolves without a discard choice")
    void damageToEmptyHandResolves() {
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(bolas.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining to pay {U}{B}{R} sacrifices Nicol Bolas")
    void declineSacrifices() {
        harness.addToBattlefield(player1, new NicolBolas());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Nicol Bolas");
    }

    @Test
    @DisplayName("Paying {U}{B}{R} keeps Nicol Bolas and spends the mana")
    void payKeeps() {
        harness.addToBattlefield(player1, new NicolBolas());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Nicol Bolas");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Dealing combat damage to an opponent makes them discard their whole hand")
    void combatDamageDiscardsHand() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new FlyingMen(), new FlyingMen()));
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        bolas.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // The whole hand (both cards) was discarded to the graveyard.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Flying Men"));
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also makes them discard their whole hand")
    void noncombatDamageDiscardsHand() {
        harness.setHand(player2, List.of(new FlyingMen(), new FlyingMen()));
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(bolas.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Flying Men"));
    }

    @Test
    @DisplayName("Damage to Nicol Bolas's controller does not trigger the discard ability")
    void damageToControllerDoesNotDiscard() {
        harness.setHand(player1, List.of(new FlyingMen(), new FlyingMen()));
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(bolas.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Flying Men"));
    }
}
