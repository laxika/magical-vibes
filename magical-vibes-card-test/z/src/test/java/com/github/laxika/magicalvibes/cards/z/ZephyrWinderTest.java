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

    @Test
    void combatDamageCanUntapWinderItself() {
        Permanent attacker = attackWithWinder();
        attacker.tap();

        resolveCombat();

        harness.handlePermanentChosen(player1, attacker.getId());
        assertThat(attacker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void combatDamageCanTargetAnUntappedCreature() {
        attackWithWinder();
        Permanent target = addCreatureReady(player2, new ZephyrWinder());

        resolveCombat();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void removingSourceDoesNotStopUntappingChosenCreature() {
        Permanent attacker = attackWithWinder();
        Permanent target = addCreatureReady(player1, new ZephyrWinder());
        target.tap();

        resolveCombat();

        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void removedTargetDoesNotUntapAnotherCreature() {
        Permanent attacker = attackWithWinder();
        attacker.tap();
        Permanent target = addCreatureReady(player2, new ZephyrWinder());
        target.tap();

        resolveCombat();

        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent attackWithWinder() {
        Permanent winder = addCreatureReady(player1, new ZephyrWinder());
        winder.setAttacking(true);
        winder.setAttackTarget(player2.getId());
        return winder;
    }
}
