package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyndicateEnforcer.class, ArmoredTransport.class, PropheticPrism.class})
class SyndicateEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell offers Extort and paying drains the opponent")
    void payingExtortDrainsOpponent() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.setHand(player1, List.of(new ArmoredTransport()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining Extort does nothing")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.setHand(player1, List.of(new ArmoredTransport()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Syndicate Enforcer does not trigger for an opponent's spell")
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ArmoredTransport()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Extort goes on the stack before its payment decision")
    void paymentDecisionWaitsForResolution() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.setHand(player1, List.of(new ArmoredTransport()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining an affordable extort payment preserves mana and life totals")
    void decliningAffordableExtortPreservesMana() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.setHand(player1, List.of(new ArmoredTransport()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colorless mana cannot pay for extort")
    void colorlessManaCannotPayForExtort() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.setHand(player1, List.of(new ArmoredTransport()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Syndicate Enforcer does not trigger its own extort")
    void castingEnforcerDoesNotTriggerItsOwnExtort() {
        harness.setHand(player1, List.of(new SyndicateEnforcer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Syndicate Enforcer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a noncreature spell also triggers extort")
    void noncreatureSpellTriggersExtort() {
        harness.addToBattlefield(player1, new SyndicateEnforcer());
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
    }
}
