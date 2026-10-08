package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.cards.e.EtchedChampion;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({VenserTheSojourner.class, GoldMyr.class, OriginSpellbomb.class, EtchedChampion.class})
class VenserTheSojournerTest extends BaseCardTest {

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

    @Test
    @DisplayName("+2 exiles own creature and it returns at end step")
    void plusTwoExilesAndReturnsOwnCreature() {
        Permanent venser = addReadyVenser(player1);
        harness.addToBattlefield(player1, new GoldMyr());
        UUID myrId = harness.getPermanentId(player1, "Gold Myr");

        harness.activateAbility(player1, 0, 0, null, myrId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Loyalty should be 3 + 2 = 5
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        // Myr should be exiled
        harness.assertNotOnBattlefield(player1, "Gold Myr");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        // Resolve the delayed return at the next end step.
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Gold Myr");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("+2 cannot target opponent's permanent")
    void plusTwoCannotTargetOpponentPermanent() {
        addReadyVenser(player1);
        harness.addToBattlefield(player2, new GoldMyr());
        UUID opponentMyrId = harness.getPermanentId(player2, "Gold Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentMyrId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+2 can target own non-creature permanent")
    void plusTwoCanTargetOwnNonCreaturePermanent() {
        Permanent venser = addReadyVenser(player1);
        // Add a second Venser-owned permanent (an artifact)
        OriginSpellbomb spellbomb = new OriginSpellbomb();
        harness.addToBattlefield(player1, spellbomb);
        UUID spellbombId = harness.getPermanentId(player1, "Origin Spellbomb");

        harness.activateAbility(player1, 0, 0, null, spellbombId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Origin Spellbomb");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);
    }

    @Test
    @DisplayName("-1 makes all creatures unblockable")
    void minusOneMakesCreaturesUnblockable() {
        Permanent venser = addReadyVenser(player1);
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player2, new GoldMyr());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Loyalty should be 3 - 1 = 2
        assertThat(venser.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        // All creatures on all battlefields should be unblockable
        gd.forEachPermanent((playerId, perm) -> {
            if (gqs.isCreature(gd, perm)) {
                assertThat(gqs.hasCantBeBlocked(gd, perm)).isTrue();
            }
        });
    }

    @Test
    @DisplayName("-1 does not affect non-creature permanents")
    void minusOneDoesNotAffectNonCreatures() {
        addReadyVenser(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Venser himself (a planeswalker) should not be affected
        Permanent venserPerm = findPermanent(player1, "Venser, the Sojourner");
        assertThat(gqs.hasCantBeBlocked(gd, venserPerm)).isFalse();
    }

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
        harness.assertNotOnBattlefield(player1, "Venser, the Sojourner");
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
        harness.addToBattlefield(player2, new GoldMyr());
        UUID myrId = harness.getPermanentId(player2, "Gold Myr");

        // Cast a creature spell - this should trigger the emblem
        harness.setHand(player1, List.of(new GoldMyr()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        // Game should be awaiting permanent choice for the emblem trigger target
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();

        // Choose the opponent's Myr as the target
        harness.handlePermanentChosen(player1, myrId);

        // Emblem trigger should be on stack (on top of creature spell)
        assertThat(gd.stack).hasSize(2);

        // Resolve the emblem trigger
        harness.passBothPriorities();

        // Opponent's Myr should be exiled
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
    }

    @Test
    @DisplayName("Emblem does not trigger when opponent casts a spell")
    void emblemDoesNotTriggerForOpponentSpells() {
        addReadyVenser(player1);
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), new VenserTheSojourner());
        gd.emblems.add(emblem);

        harness.addToBattlefield(player1, new GoldMyr());

        // Opponent casts a spell; the emblem should not trigger.
        harness.setHand(player2, List.of(new GoldMyr()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);

        // Should NOT be awaiting permanent choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isFalse();
        // Just the creature spell on stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate -8 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyVenser(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    void plusTwoCanExileVenserItselfAndReturnWithStartingLoyalty() {
        Permanent venser = addReadyVenser(player1);
        harness.activateAbility(player1, 0, 0, null, venser.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Venser, the Sojourner");

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Venser, the Sojourner");
        assertThat(returned.getId()).isNotEqualTo(venser.getId());
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void plusTwoReturnsOwnedPermanentFromOpponentUnderYourControl() {
        addReadyVenser(player1);
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        gd.stolenCreatures.put(myr.getId(), player1.getId());

        harness.activateAbility(player1, 0, 0, null, myr.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
    }

    @Test
    void plusTwoCannotTargetOpponentOwnedPermanentYouControl() {
        addReadyVenser(player1);
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        gd.stolenCreatures.put(myr.getId(), player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, myr.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusOneAppliesToCreaturesEnteringLaterAndExpiresNextTurn() {
        addReadyVenser(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent myr = harness.enterBattlefieldAndReturn(player1, new GoldMyr());
        assertThat(gqs.hasCantBeBlocked(gd, myr)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasCantBeBlocked(gd, myr)).isFalse();
    }

    @Test
    void emblemCanExilePermanentWithProtectionFromEveryColor() {
        Permanent venser = addReadyVenser(player1);
        venser.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new EtchedChampion());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new OriginSpellbomb());
        harness.setHand(player1, List.of(new GoldMyr()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, champion.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Etched Champion");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(champion.getCard());
    }

    private Permanent addReadyVenser(Player player) {
        VenserTheSojourner card = new VenserTheSojourner();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
