package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SkarrganPitSkulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroningBureaucrats.class, DryadSophisticate.class, SkarrganPitSkulk.class})
class DroningBureaucratsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with mana value X can't attack")
    void matchingCreaturesCannotAttack() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent attackingCreature = addCreatureReady(player1, new DryadSophisticate());

        activate(bureaucrats, 2);

        assertThatThrownBy(() -> declareAttack(attackingCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with mana value X can't block")
    void matchingCreaturesCannotBlock() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent attacker = addCreatureReady(player1, new SkarrganPitSkulk());
        Permanent blockingCreature = addCreatureReady(player2, new DryadSophisticate());

        activate(bureaucrats, 2);

        assertThatThrownBy(() -> declareBlock(attacker, blockingCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Creatures with a different mana value are unaffected")
    void differentManaValueIsUnaffected() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent unaffectedCreature = addCreatureReady(player1, new SkarrganPitSkulk());

        activate(bureaucrats, 2);

        assertThatCode(() -> declareAttack(unaffectedCreature)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent creature = addCreatureReady(player1, new DryadSophisticate());

        activate(bureaucrats, 2);
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(creature)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Matching creatures that enter after resolution are also restricted")
    void matchingCreatureEnteringAfterResolutionCannotAttack() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());

        activate(bureaucrats, 2);

        Permanent lateCreature = addCreatureReady(player2, new DryadSophisticate());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(lateCreature))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private void activate(Permanent bureaucrats, int xValue) {
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bureaucrats), xValue, null);
        harness.passBothPriorities();
    }

    private void declareAttack(Permanent creature) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
