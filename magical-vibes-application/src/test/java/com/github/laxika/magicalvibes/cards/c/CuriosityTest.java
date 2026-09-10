package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Curiosity.class, GrizzlyBears.class, AnabaShaman.class})
class CuriosityTest extends BaseCardTest {

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Enchanted creature dealing combat damage presents may-draw choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        declareAttackers(List.of(0));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may-draw after combat damage draws a card")
    void acceptingMayDrawsCard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        declareAttackers(List.of(0));

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining may-draw after combat damage does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        declareAttackers(List.of(0));

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when enchanted creature is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        declareAttackers(List.of(0));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender still takes combat damage regardless of may choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachCuriosity(player1, creature);
        declareAttackers(List.of(0));

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Grizzly Bears is 2/2, should deal 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent presents a may-draw choice")
    void noncombatDamageToOpponentPresentsMayChoice() {
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        attachCuriosity(player1, shaman);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Noncombat damage to its controller does not trigger Curiosity")
    void noncombatDamageToControllerDoesNotTrigger() {
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        attachCuriosity(player1, shaman);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Curiosity does not trigger when an opponent's creature damages its controller")
    void opponentCreatureDamagingCuriosityControllerDoesNotTrigger() {
        Permanent shaman = addCreatureReady(player2, new AnabaShaman());
        attachCuriosity(player1, shaman);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Casting and attachment =====

    @Test
    @DisplayName("Casting Curiosity attaches it to the target creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Curiosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Curiosity
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Curiosity fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Curiosity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(Curiosity.class::isInstance);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof Curiosity);
    }

    // ===== Helpers =====

    private void attachCuriosity(Player controller, Permanent creature) {
        Permanent curiosity = harness.addToBattlefieldAndReturn(controller, new Curiosity());
        curiosity.setAttachedTo(creature.getId());
    }
}
