package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SliverConstruct;
import com.github.laxika.magicalvibes.cards.a.ArmoredCancrix;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasReaver.class, Forest.class, SliverConstruct.class, ArmoredCancrix.class})
class LilianasReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard and creates a tapped 2/2 Zombie")
    void combatDamageDiscardsAndCreatesZombie() {
        addAttackingReaver(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new SliverConstruct(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        assertThat(findPermanents(player1, "Zombie"))
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(2);
                    assertThat(p.getCard().getToughness()).isEqualTo(2);
                    assertThat(p.isTapped()).isTrue();
                });
    }

    @Test
    @DisplayName("Empty-handed damaged player still gives the controller a Zombie")
    void emptyHandStillCreatesZombie() {
        addAttackingReaver(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(findPermanents(player1, "Zombie"))
                .hasSize(1);
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches the player")
    void noTriggerWhenBlocked() {
        addAttackingReaver(player1);
        Permanent blocker = addCreatureReady(player2, new SliverConstruct());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Deathtouch kills a blocker with more toughness than the damage assigned")
    void deathtouchKillsLargerBlocker() {
        addAttackingReaver(player1);
        Permanent blocker = addCreatureReady(player2, new ArmoredCancrix());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        harness.assertInGraveyard(player2, "Armored Cancrix");
        harness.assertOnBattlefield(player1, "Liliana's Reaver");
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Player two's Reaver makes player one discard and gives player two the token")
    void otherControllerReceivesToken() {
        addAttackingReaver(player2);
        harness.setHand(player1, List.of(new Forest(), new SliverConstruct()));

        resolveCombat(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player2, "Zombie")).singleElement()
                .satisfies(token -> assertThat(token.isTapped()).isTrue());
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("The combat damage trigger resolves after its source leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent reaver = addAttackingReaver(player1);
        harness.setHand(player2, List.of(new Forest(), new SliverConstruct()));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        gd.playerBattlefields.get(player1.getId()).remove(reaver);
        gd.playerGraveyards.get(player1.getId()).add(reaver.getCard());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    private Permanent addAttackingReaver(Player player) {
        Permanent reaver = addCreatureReady(player, new LilianasReaver());
        reaver.setAttacking(true);
        return reaver;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
