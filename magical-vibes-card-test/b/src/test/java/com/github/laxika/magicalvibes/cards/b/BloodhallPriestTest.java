package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodhallPriest.class, GrizzlyBears.class, RavensCrime.class})
class BloodhallPriestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to any target when hand is empty")
    void etbDeals2DamageWhenHandEmpty() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBloodhallPriestAlone();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals 2 damage to a player when hand is empty")
    void etbDeals2DamageToPlayerWhenHandEmpty() {
        harness.setLife(player2, 20);
        castBloodhallPriestAlone();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does not trigger when controller still has cards in hand")
    void etbDoesNotTriggerWithCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodhallPriest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Bloodhall Priest");
    }

    @Test
    @DisplayName("Attack trigger deals 2 damage when hand is empty")
    void attackDeals2DamageWhenHandEmpty() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BloodhallPriest());
        harness.setHand(player1, List.of());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // 2 trigger damage + 4 combat damage from the 4/4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Attack does not trigger when controller has cards in hand")
    void attackDoesNotTriggerWithCardsInHand() {
        addCreatureReady(player1, new BloodhallPriest());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Bloodhall Priest"));
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Discarding Bloodhall Priest exiles it and offers madness cast")
    void discardTriggersMadness() {
        BloodhallPriest priest = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(priest.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting madness cast pays {1}{B}{R} and puts the creature on the battlefield")
    void acceptingMadnessCastsCreature() {
        BloodhallPriest priest = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        // Madness cast resolves onto BF; empty hand → ETB any-target prompt
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(priest.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB deals no damage if controller gains a card before resolution")
    void etbRechecksEmptyHandAtResolution() {
        harness.setLife(player2, 20);
        castBloodhallPriestAlone();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new BloodhallPriest()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Bloodhall Priest");
    }

    @Test
    @DisplayName("Attack deals no trigger damage if controller gains a card before resolution")
    void attackRechecksEmptyHandAtResolution() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BloodhallPriest());
        harness.setHand(player1, List.of());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new BloodhallPriest()));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("ETB may target its own controller")
    void etbCanDamageController() {
        harness.setLife(player1, 20);
        castBloodhallPriestAlone();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining madness moves the discarded card from exile to graveyard")
    void decliningMadnessPutsCardInGraveyard() {
        BloodhallPriest priest = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(priest.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(priest.getId()));
        harness.assertNotOnBattlefield(player1, "Bloodhall Priest");
    }

    private void castBloodhallPriestAlone() {
        harness.castFromHand(player1, new BloodhallPriest(), "{2}{B}{R}");
    }

    private BloodhallPriest discardViaRavensCrime() {
        BloodhallPriest priest = new BloodhallPriest();
        harness.setHand(player1, List.of(priest));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return priest;
    }
}
