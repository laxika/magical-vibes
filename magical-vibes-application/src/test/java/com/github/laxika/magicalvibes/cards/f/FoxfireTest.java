package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Foxfire.class, GrizzlyBears.class, Incinerate.class})
class FoxfireTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target attacking creature")
    void untapsTargetAttacker() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();

        castFoxfire(attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a player")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castFoxfire(attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature by a blocker")
    void preventsCombatDamageDealtToCreature() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 0);

        castFoxfire(attacker);
        resolveCombat();

        // A 3/3 blocker would normally kill the 2/2 attacker; combat damage to it is prevented.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a blocker")
    void preventsCombatDamageDealtByCreatureToBlocker() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addBlocker(player2, 1, 1, 0);

        castFoxfire(attacker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Noncombat damage to the target creature is not prevented")
    void doesNotPreventNoncombatDamage() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castFoxfire(attacker);

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(attacker.getId());
        assertThat(gd.creaturesWithAllDamagePrevented).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Noncombat damage to the target creature still applies")
    void noncombatDamageStillAppliesToTargetCreature() {
        Permanent attacker = addAttacker(player1, player2, 4, 4);

        castFoxfire(attacker);

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Schedules a draw at the next turn's upkeep")
    void drawsCardAtNextUpkeep() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castFoxfire(attacker);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Foxfire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by an opponent")
    void canTargetOpponentAttacker() {
        Permanent attacker = addAttacker(player2, player1, 2, 2);
        attacker.tap();

        castFoxfire(attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not schedule the draw if the target is gone before resolution")
    void doesNotScheduleDrawWhenTargetLeavesBeforeResolution() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        harness.setHand(player1, List.of(new Foxfire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        harness.assertInGraveyard(player1, "Foxfire");
    }

    private void castFoxfire(Permanent target) {
        harness.setHand(player1, List.of(new Foxfire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, bears);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, bears);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        return perm;
    }
}
