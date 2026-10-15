package com.github.laxika.magicalvibes.cards.j;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;

import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.cards.s.SirenStormtamer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({JaceCunningCastaway.class, PerilousVoyage.class, SirenStormtamer.class, Island.class})
class JaceCunningCastawayTest extends BaseCardTest {

    @Test
    @DisplayName("+1 ability increases loyalty and registers delayed loot trigger")
    void plusOneRegistersDelayedLootTrigger() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // 3 + 1
        assertThat(gd.getDelayedActions(DelayedCombatDamageLoot.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCombatDamageLoot.class).getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(DelayedCombatDamageLoot.class).getFirst().drawAmount()).isEqualTo(1);
        assertThat(gd.getDelayedActions(DelayedCombatDamageLoot.class).getFirst().discardAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 delayed trigger data is correctly stored with source card reference")
    void plusOneDelayedTriggerStoresSourceCard() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getDelayedActions(DelayedCombatDamageLoot.class)).hasSize(1);
        DelayedCombatDamageLoot loot = gd.getDelayedActions(DelayedCombatDamageLoot.class).getFirst();
        assertThat(loot.sourceCard()).isNotNull();
        assertThat(loot.sourceCard()).isSameAs(jace.getCard());
    }

    @Test
    @DisplayName("-2 ability creates a 2/2 Illusion token and decreases loyalty")
    void minusTwoCreatesIllusionToken() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 3 - 2

        Permanent illusionToken = findPermanent(player1, "Illusion");
        assertThat(illusionToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(illusionToken.getCard().getPower()).isEqualTo(2);
        assertThat(illusionToken.getCard().getToughness()).isEqualTo(2);
        assertThat(illusionToken.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(illusionToken.getCard().getSubtypes()).containsExactly(CardSubtype.ILLUSION);
        assertThat(illusionToken.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("-2 Illusion token sacrifices itself when it becomes the target of a spell")
    void illusionTokenSacrificesWhenTargeted() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent illusionToken = findPermanent(player1, "Illusion");

        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, illusionToken.getId());

        // The non-targeting sacrifice trigger resolves above Perilous Voyage, sacrificing the token.
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(illusionToken.getId()));
    }

    @Test
    @DisplayName("Cannot activate -2 when loyalty is only 1")
    void cannotActivateMinusTwoWithInsufficientLoyalty() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("-5 ability creates two non-legendary token copies of Jace")
    void minusFiveCreatesTwoNonLegendaryCopies() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Original Jace should have 0 loyalty and be gone (SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getCard().isToken() && p.getCard().getName().equals("Jace, Cunning Castaway"))
                .toList()).isEmpty();

        // Two token copies should exist
        List<Permanent> jaceCopies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Jace, Cunning Castaway"))
                .toList();
        assertThat(jaceCopies).hasSize(2);

        for (Permanent copy : jaceCopies) {
            // Tokens are NOT legendary
            assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
            // Tokens should have loyalty counters
            assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // initial loyalty of Jace
            // Tokens are planeswalkers
            assertThat(copy.getCard().getType()).isEqualTo(CardType.PLANESWALKER);
        }
    }

    @Test
    @DisplayName("Cannot activate -5 when loyalty is only 3")
    void cannotActivateMinusFiveWithInsufficientLoyalty() {
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyJace(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("+1 loots once for simultaneous combat damage by multiple creatures")
    void simultaneousCombatDamageLootsOnce() {
        addReadyJace(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn, new Island()));
        Permanent first = addCreatureReady(player1, new SirenStormtamer());
        Permanent second = addCreatureReady(player1, new SirenStormtamer());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("+1 does not trigger for combat damage to a planeswalker")
    void planeswalkerCombatDamageDoesNotLoot() {
        addReadyJace(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new JaceCunningCastaway());
        defender.setCounterCount(CounterType.LOYALTY, 3);
        Permanent attacker = addCreatureReady(player1, new SirenStormtamer());
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(defender.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Token copies can activate their own loyalty abilities immediately")
    void tokenCopiesCanEachActivateLoyaltyAbilities() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion")).hasSize(2);
    }

    @Test
    @DisplayName("+1 delayed loot remains active after Jace leaves the battlefield")
    void delayedLootSurvivesJaceLeaving() {
        Permanent jace = addReadyJace(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, jace.getId());
        harness.assertNotOnBattlefield(player1, "Jace, Cunning Castaway");
        harness.setHand(player1, List.of());
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn, new Island()));
        Permanent attacker = addCreatureReady(player1, new SirenStormtamer());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyJace(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceCunningCastaway());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
