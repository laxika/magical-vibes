package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class VenserTheSojournerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeAbilities() {
        VenserTheSojourner card = new VenserTheSojourner();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    

    

    

    // ===== Casting =====

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new VenserTheSojourner()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Venser, the Sojourner"));
        Permanent venser = bf.stream().filter(p -> p.getCard().getName().equals("Venser, the Sojourner")).findFirst().orElseThrow();
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    // ===== +2 ability: Exile target permanent you own, return at end step =====

    @Test
    @DisplayName("+2 exiles own creature and it returns at end step")
    void plusTwoExilesAndReturnsOwnCreature() {
        Permanent venser = addReadyVenser(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Loyalty should be 3 + 2 = 5
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        // Bears should be exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        // Advance to end step — bears should return
        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("+2 cannot target opponent's permanent")
    void plusTwoCannotTargetOpponentPermanent() {
        addReadyVenser(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+2 can target own non-creature permanent")
    void plusTwoCanTargetOwnNonCreaturePermanent() {
        Permanent venser = addReadyVenser(player1);
        // Add a second Venser-owned permanent (an artifact)
        com.github.laxika.magicalvibes.cards.g.GoldMyr goldMyr = new com.github.laxika.magicalvibes.cards.g.GoldMyr();
        harness.addToBattlefield(player1, goldMyr);
        UUID goldMyrId = harness.getPermanentId(player1, "Gold Myr");

        harness.activateAbility(player1, 0, 0, null, goldMyrId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);
    }

    // ===== -1 ability: Creatures can't be blocked this turn =====

    @Test
    @DisplayName("-1 makes all creatures unblockable")
    void minusOneMakesCreaturesUnblockable() {
        Permanent venser = addReadyVenser(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Loyalty should be 3 - 1 = 2
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        // All creatures on all battlefields should be unblockable
        gd.forEachPermanent((playerId, perm) -> {
            if (gqs.isCreature(gd, perm)) {
                assertThat(perm.isCantBeBlocked()).isTrue();
            }
        });
    }

    @Test
    @DisplayName("-1 does not affect non-creature permanents")
    void minusOneDoesNotAffectNonCreatures() {
        addReadyVenser(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Venser himself (a planeswalker) should not be affected
        Permanent venserPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Venser, the Sojourner"))
                .findFirst().orElseThrow();
        assertThat(venserPerm.isCantBeBlocked()).isFalse();
    }

    // ===== -8 ability: Emblem =====

    @Test
    @DisplayName("-8 creates emblem with correct effect")
    void minusEightCreatesEmblem() {
        Permanent venser = addReadyVenser(player1);
        venser.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);
        assertThat(emblem.staticEffects().getFirst()).isInstanceOf(ExileTargetOnControllerSpellCastEffect.class);
        assertThat(emblem.sourceCard()).isNotNull();
    }

    @Test
    @DisplayName("Emblem persists after Venser dies (loyalty goes to 0)")
    void emblemPersistsAfterVenserDies() {
        Permanent venser = addReadyVenser(player1);
        venser.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Venser should be gone (8 - 8 = 0 loyalty)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Venser, the Sojourner"));
        // Emblem persists
        assertThat(gd.emblems).hasSize(1);
    }

    @Test
    @DisplayName("Emblem triggers when controller casts a spell, allowing exile of target permanent")
    void emblemTriggersOnSpellCast() {
        addReadyVenser(player1);
        // Manually create the emblem (simulating ultimate already resolved)
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), new VenserTheSojourner());
        gd.emblems.add(emblem);

        // Add a target permanent on opponent's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Cast a creature spell - this should trigger the emblem
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Game should be awaiting permanent choice for the emblem trigger target
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();

        // Choose the opponent's bears as the target
        harness.handlePermanentChosen(player1, bearsId);

        // Emblem trigger should be on stack (on top of creature spell)
        assertThat(gd.stack).hasSize(2);

        // Resolve the emblem trigger
        harness.passBothPriorities();

        // Opponent's bears should be exiled
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Emblem does not trigger when opponent casts a spell")
    void emblemDoesNotTriggerForOpponentSpells() {
        addReadyVenser(player1);
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), new VenserTheSojourner());
        gd.emblems.add(emblem);

        harness.addToBattlefield(player1, new GrizzlyBears());

        // Opponent casts a spell — emblem should NOT trigger
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);

        // Should NOT be awaiting permanent choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isFalse();
        // Just the creature spell on stack
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate -8 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyVenser(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadyVenser(Player player) {
        VenserTheSojourner card = new VenserTheSojourner();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
