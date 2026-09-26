package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NicolBolas.class, DurkwoodBoars.class, HermeticStudy.class})
class NicolBolasTest extends BaseCardTest {

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
        harness.setHand(player2, List.of(new DurkwoodBoars(), new DurkwoodBoars()));
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        bolas.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // The whole hand (both cards) was discarded to the graveyard.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Durkwood Boars"));
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also makes them discard their whole hand")
    void noncombatDamageDiscardsHand() {
        harness.setHand(player2, List.of(new DurkwoodBoars(), new DurkwoodBoars()));
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(bolas.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Durkwood Boars"));
    }

    @Test
    @DisplayName("Damage to Nicol Bolas's controller does not trigger the discard ability")
    void damageToControllerDoesNotDiscard() {
        harness.setHand(player1, List.of(new DurkwoodBoars(), new DurkwoodBoars()));
        Permanent bolas = addCreatureReady(player1, new NicolBolas());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(bolas.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(c -> c.getName().equals("Durkwood Boars"));
    }
}
