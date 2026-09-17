package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Camel.class, Desert.class, GrizzlyBears.class})
class CamelTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents Desert damage to itself while attacking")
    void preventsDesertDamageToItselfWhileAttacking() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent camel = addCreatureReady(player2, new Camel());
        camel.setAttacking(true);

        prepareDesertDamage(camel);
        harness.activateAbility(player1, battlefieldIndex(player1, desert), 1, null, camel.getId());
        harness.passBothPriorities();

        assertThat(camel.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Camel");
    }

    @Test
    @DisplayName("Prevents Desert damage to another creature in its band")
    void preventsDesertDamageToBandedCreature() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent camel = addCreatureReady(player2, new Camel());
        Permanent bandmate = addCreatureReady(player2, new GrizzlyBears());
        UUID bandId = UUID.randomUUID();
        camel.setAttacking(true);
        camel.setBandId(bandId);
        bandmate.setAttacking(true);
        bandmate.setBandId(bandId);

        prepareDesertDamage(bandmate);
        harness.activateAbility(player1, battlefieldIndex(player1, desert), 1, null, bandmate.getId());
        harness.passBothPriorities();

        assertThat(bandmate.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not protect an attacking creature outside its band")
    void doesNotProtectCreatureOutsideBand() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent camel = addCreatureReady(player2, new Camel());
        Permanent otherAttacker = addCreatureReady(player2, new GrizzlyBears());
        camel.setAttacking(true);
        camel.setBandId(UUID.randomUUID());
        otherAttacker.setAttacking(true);
        otherAttacker.setBandId(UUID.randomUUID());

        prepareDesertDamage(otherAttacker);
        harness.activateAbility(player1, battlefieldIndex(player1, desert), 1, null, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getMarkedDamage()).isEqualTo(1);
    }

    private void prepareDesertDamage(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        assertThat(target.isAttacking()).isTrue();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
