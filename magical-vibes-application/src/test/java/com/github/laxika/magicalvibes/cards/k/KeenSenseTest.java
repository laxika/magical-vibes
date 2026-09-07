package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({KeenSense.class, Forest.class, GrizzlyBears.class, ProdigalPyromancer.class})
class KeenSenseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature dealing combat damage presents a may-draw choice")
    void combatDamagePresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachKeenSense(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw choice draws a card")
    void acceptingMayDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachKeenSense(player1, creature);
        creature.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw choice does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachKeenSense(player1, creature);
        creature.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also presents a may-draw choice")
    void noncombatDamagePresentsMayChoice() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        attachKeenSense(player1, pyromancer);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("No trigger occurs when combat damage is prevented by a blocker")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachKeenSense(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting Keen Sense attaches it to the target creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KeenSense()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Keen Sense")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Keen Sense fizzles if its target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KeenSense()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Keen Sense");
        harness.assertNotOnBattlefield(player1, "Keen Sense");
    }

    private void attachKeenSense(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new KeenSense());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
