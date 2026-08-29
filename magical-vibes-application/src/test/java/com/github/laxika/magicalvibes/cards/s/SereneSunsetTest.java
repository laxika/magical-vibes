package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SereneSunset.class, GrizzlyBears.class, Forest.class, ZuranSpellcaster.class})
class SereneSunsetTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from the targeted creatures only")
    void preventsCombatDamageFromTargetedCreaturesOnly() {
        harness.setLife(player2, 20);
        Permanent targeted = addAttacker();
        Permanent untargeted = addAttacker();

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(targeted.getId()));
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Requires exactly X creature targets")
    void requiresExactlyXTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new ZuranSpellcaster());
        targeted.setSummoningSick(false);

        harness.setHand(player1, List.of(new SereneSunset()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantForX(player1, 0, 1, List.of(targeted.getId()));
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
