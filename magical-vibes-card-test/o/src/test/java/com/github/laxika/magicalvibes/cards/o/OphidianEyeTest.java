package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OphidianEye.class, AshcoatBear.class, FledglingMawcor.class})
class OphidianEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature dealing combat damage presents a may-draw choice")
    void combatDamagePresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        attachOphidianEye(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw choice draws a card")
    void acceptingMayDrawsCard() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        attachOphidianEye(player1, creature);
        creature.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw choice does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        attachOphidianEye(player1, creature);
        creature.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent presents a may-draw choice")
    void noncombatDamageToOpponentPresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new FledglingMawcor());
        attachOphidianEye(player1, creature);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Damage to the enchanted creature's controller does not trigger Ophidian Eye")
    void damageToEnchantedCreatureControllerDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new FledglingMawcor());
        attachOphidianEye(player1, creature);

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Damage to Ophidian Eye's controller by an opponent's enchanted creature does not trigger")
    void opponentCreatureDamagingEyeControllerDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new FledglingMawcor());
        attachOphidianEye(player1, creature);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A blocked enchanted creature that deals no damage to a player does not trigger")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        attachOphidianEye(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting Ophidian Eye attaches it to the target creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new OphidianEye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof OphidianEye
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Ophidian Eye fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new OphidianEye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ophidian Eye");
        harness.assertNotOnBattlefield(player1, "Ophidian Eye");
    }

    @Test
    @DisplayName("An opposing creature damaging its own controller draws for Ophidian Eye's controller")
    void opponentCreatureDamagingItsControllerDrawsForAuraController() {
        Permanent creature = addCreatureReady(player2, new FledglingMawcor());
        attachOphidianEye(player1, creature);
        harness.forceActivePlayer(player2);
        int auraControllerHandSize = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, null, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHandSize);
    }

    @Test
    @DisplayName("Combat damage to Ophidian Eye's controller does not trigger")
    void combatDamageToAuraControllerDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        attachOphidianEye(player1, creature);
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to a creature does not trigger Ophidian Eye")
    void noncombatDamageToCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new FledglingMawcor());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        attachOphidianEye(player1, creature);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows Ophidian Eye to resolve in response to an opposing creature's damage ability")
    void flashRespondsToDamageAbility() {
        Permanent creature = addCreatureReady(player2, new FledglingMawcor());
        harness.setHand(player1, List.of(new OphidianEye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        int creatureControllerHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passPriority(player2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ophidian Eye").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertLife(player2, 20);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHandSize);
    }

    private void attachOphidianEye(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new OphidianEye());
        aura.setAttachedTo(creature.getId());
    }
}
