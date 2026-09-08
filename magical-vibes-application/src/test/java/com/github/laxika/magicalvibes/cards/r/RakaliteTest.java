package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RakaliteTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage to a targeted player")
    void preventsNextDamageToPlayer() {
        harness.addToBattlefield(player1, new Rakalite());
        Permanent firstSpellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        Permanent secondSpellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(firstSpellcaster.isTapped()).isTrue();
        assertThat(secondSpellcaster.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents the next damage to a targeted creature")
    void preventsNextDamageToCreature() {
        harness.addToBattlefield(player1, new Rakalite());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent memnite = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, memnite.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, memnite.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Memnite");
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the beginning of the next end step")
    void returnsSelfToHandAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player1, new Rakalite());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rakalite");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rakalite");
        harness.assertInHand(player1, "Rakalite");
    }
}
