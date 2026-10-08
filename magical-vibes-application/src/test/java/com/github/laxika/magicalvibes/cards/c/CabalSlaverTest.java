package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalSlaver.class, SkirkProspector.class})
class CabalSlaverTest extends BaseCardTest {

    @Test
    @DisplayName("A Goblin dealing combat damage makes the damaged player discard a card")
    void goblinCombatDamageForcesDamagedPlayerToDiscard() {
        harness.setHand(player2, List.of(new CabalSlaver()));
        harness.addToBattlefield(player1, new CabalSlaver());

        Permanent goblin = addCreatureReady(player1, new SkirkProspector());
        goblin.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Cabal Slaver");
    }

    @Test
    @DisplayName("An opposing Goblin dealing combat damage also triggers Cabal Slaver")
    void opposingGoblinCombatDamageForcesControllerToDiscard() {
        harness.setHand(player1, List.of(new CabalSlaver()));
        harness.addToBattlefield(player1, new CabalSlaver());

        Permanent goblin = addCreatureReady(player2, new SkirkProspector());
        goblin.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cabal Slaver");
    }

    @Test
    @DisplayName("Each Goblin dealing combat damage creates a separate discard trigger")
    void eachGoblinCombatDamageCreatesSeparateDiscardTrigger() {
        harness.setHand(player2, List.of(new CabalSlaver(), new CabalSlaver()));
        harness.addToBattlefield(player1, new CabalSlaver());

        Permanent firstGoblin = addCreatureReady(player1, new SkirkProspector());
        firstGoblin.setAttacking(true);
        Permanent secondGoblin = addCreatureReady(player1, new SkirkProspector());
        secondGoblin.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A non-Goblin dealing combat damage does not trigger Cabal Slaver")
    void nonGoblinCombatDamageDoesNotTrigger() {
        harness.setHand(player2, List.of(new CabalSlaver()));
        harness.addToBattlefield(player1, new CabalSlaver());

        Permanent creature = addCreatureReady(player1, new CabalSlaver());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Cabal Slaver");
    }

    @Test
    @DisplayName("Cabal Slaver controls and is the source of the trigger from an opposing Goblin")
    void slaverIsSourceAndControllerOfOpposingGoblinTrigger() {
        harness.setHand(player1, List.of(new CabalSlaver()));
        Permanent slaver = harness.addToBattlefieldAndReturn(player1, new CabalSlaver());
        Permanent goblin = addCreatureReady(player2, new SkirkProspector());
        goblin.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat(player2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(slaver.getId());
    }

    @Test
    @DisplayName("Two Cabal Slavers each trigger from a single Goblin")
    void twoSlaversCauseTwoDiscards() {
        harness.setHand(player2, List.of(new CabalSlaver(), new CabalSlaver()));
        harness.addToBattlefield(player1, new CabalSlaver());
        harness.addToBattlefield(player1, new CabalSlaver());
        Permanent goblin = addCreatureReady(player1, new SkirkProspector());
        goblin.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The discard trigger resolves harmlessly when the damaged player has no cards")
    void emptyHandDoesNotRequireDiscardChoice() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new CabalSlaver());
        Permanent goblin = addCreatureReady(player1, new SkirkProspector());
        goblin.setAttacking(true);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 19);
    }

}
