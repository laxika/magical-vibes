package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenHeretic.class, GrizzlyBears.class, SoulsFire.class})
class VedalkenHereticTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to a player presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        heretic.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw draws a card")
    void acceptingMayDrawsCard() {
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        heretic.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw does not draw a card")
    void decliningMayDoesNotDraw() {
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        heretic.setAttacking(true);

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when blocked and no damage reaches the player")
    void noTriggerWhenBlocked() {
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        heretic.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Defender takes combat damage regardless of the may choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        heretic.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Vedalken Heretic is 1/1, deals 1 damage.
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent can draw a card")
    void noncombatDamageToOpponentDrawsCard() {
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(heretic.getId(), player2.getId()));

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Damage to its own controller does not trigger a draw")
    void damageToControllerDoesNotTrigger() {
        Permanent heretic = addCreatureReady(player1, new VedalkenHeretic());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(heretic.getId(), player1.getId()));

        harness.assertLife(player1, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
