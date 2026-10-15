package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessBrute.class, WalkingCorpse.class, Pacifism.class})
class RecklessBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring no attackers while Reckless Brute can attack is rejected")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new RecklessBrute());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Reckless Brute while declaring another attacker is rejected")
    void mustBeIncludedAmongAttackers() {
        addCreatureReady(player1, new RecklessBrute());
        addCreatureReady(player1, new WalkingCorpse());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Reckless Brute attacks for 3 when declared")
    void attacksForThree() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RecklessBrute());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Haste lets a summoning-sick Reckless Brute attack, so it is still forced to")
    void hasteMakesItAttackTheTurnItEnters() {
        harness.addToBattlefield(player1, new RecklessBrute());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A tapped Reckless Brute imposes no attack requirement")
    void tappedBruteIsNotForcedToAttack() {
        Permanent brute = addCreatureReady(player1, new RecklessBrute());
        brute.tap();

        declareAttackers(List.of());

        assertThat(brute.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A newly entered Reckless Brute can attack and deal combat damage")
    void hasteAllowsAttackTheTurnItEnters() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RecklessBrute());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pacifism prevents Reckless Brute from attacking despite its requirement")
    void attackRestrictionOverridesAttackRequirement() {
        Permanent brute = addCreatureReady(player1, new RecklessBrute());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        pacifism.setAttachedTo(brute.getId());
        addCreatureReady(player1, new WalkingCorpse());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));

        assertThat(brute.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Reckless Brute must attack again when untapped for another combat in the same turn")
    void mustAttackEachCombat() {
        Permanent brute = addCreatureReady(player1, new RecklessBrute());
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> declareAttackers(List.of(0)));
        brute.untap();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
