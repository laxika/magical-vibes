package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glarecaster.class, GlorySeeker.class, GoblinSharpshooter.class})
class GlarecasterTest extends BaseCardTest {

    @Test
    void redirectsTheNextDamageToTheCreature() {
        Permanent glarecaster = addCreatureReady(player1, new Glarecaster());
        Permanent destination = addCreatureReady(player1, new GlorySeeker());
        Permanent sharpshooter = addCreatureReady(player1, new GoblinSharpshooter());

        activateGlarecaster(glarecaster, destination.getId());
        harness.activateAbility(player1, indexOf(player1, sharpshooter), null, glarecaster.getId());
        harness.passBothPriorities();

        assertThat(glarecaster.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void redirectsTheNextDamageToTheController() {
        Permanent glarecaster = addCreatureReady(player1, new Glarecaster());
        Permanent destination = addCreatureReady(player1, new GlorySeeker());
        Permanent sharpshooter = addCreatureReady(player1, new GoblinSharpshooter());
        int lifeBefore = gd.getLife(player1.getId());

        activateGlarecaster(glarecaster, destination.getId());
        harness.activateAbility(player1, indexOf(player1, sharpshooter), null, player1.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void sharedShieldIsConsumedByTheFirstProtectedRecipient() {
        Permanent glarecaster = addCreatureReady(player1, new Glarecaster());
        Permanent destination = addCreatureReady(player1, new GlorySeeker());
        Permanent firstSharpshooter = addCreatureReady(player1, new GoblinSharpshooter());
        Permanent secondSharpshooter = addCreatureReady(player1, new GoblinSharpshooter());
        int lifeBefore = gd.getLife(player1.getId());

        activateGlarecaster(glarecaster, destination.getId());
        harness.activateAbility(player1, indexOf(player1, firstSharpshooter), null, glarecaster.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondSharpshooter), null, player1.getId());
        harness.passBothPriorities();

        assertThat(glarecaster.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void redirectsBothChunksWhenCreatureAndControllerAreDamagedSimultaneously() {
        Permanent glarecaster = addCreatureReady(player1, new Glarecaster());
        Permanent blockedAttacker = addCreatureReady(player2, new GlorySeeker());
        Permanent unblockedAttacker = addCreatureReady(player2, new GlorySeeker());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        activateGlarecaster(glarecaster, player2.getId());

        declareAttackers(player2, List.of(
                indexOf(player2, blockedAttacker), indexOf(player2, unblockedAttacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, glarecaster), indexOf(player2, blockedAttacker))));
        resolveCombat(player2);

        assertThat(glarecaster.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 4);
    }

    private void activateGlarecaster(Permanent glarecaster, java.util.UUID destinationId) {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, glarecaster), null, destinationId);
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
