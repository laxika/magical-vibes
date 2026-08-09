package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FendOffTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from the targeted creature")
    void preventsCombatDamageFromTargetCreature() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2);
        castFendOff(attacker);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from the targeted creature")
    void doesNotPreventNoncombatDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castFendOff(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, false)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new FendOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new FendOff()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fend Off");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castFendOff(Permanent target) {
        harness.setHand(player1, List.of(new FendOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
