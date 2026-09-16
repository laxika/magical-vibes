package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrWinder.class, Forest.class})
class ZephyrWinderTest extends BaseCardTest {

    @Test
    void combatDamageUntapsUpToOneTargetCreature() {
        attackWithWinder();
        Permanent target = addCreatureReady(player2, new ZephyrWinder());
        target.tap();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void combatDamageMayUntapNoCreature() {
        attackWithWinder();
        Permanent target = addCreatureReady(player2, new ZephyrWinder());
        target.tap();

        resolveCombat();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void combatDamageCannotTargetANoncreature() {
        Permanent attacker = attackWithWinder();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveCombat();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
    }

    private Permanent attackWithWinder() {
        Permanent winder = addCreatureReady(player1, new ZephyrWinder());
        winder.setAttacking(true);
        winder.setAttackTarget(player2.getId());
        return winder;
    }
}
